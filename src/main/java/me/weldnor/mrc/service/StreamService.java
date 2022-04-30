package me.weldnor.mrc.service;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.pojo.StreamSession;
import me.weldnor.mrc.utils.WebSocketUtil;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StreamService {
    private final StreamSessionService streamSessionService;

    private final MediaPipeline pipeline;

    public StreamService(StreamSessionService streamSessionService, KurentoClient kurentoClient) {
        this.streamSessionService = streamSessionService;
        this.pipeline = kurentoClient.createMediaPipeline();
    }

    public void onJoinMessage(String userId, String roomId, WebSocketSession webSocketSession) {
        // save session
        StreamSession session = new StreamSession();
        session.setUserId(userId);
        session.setRoomId(roomId);
        session.setWebSocketSession(webSocketSession);
        session.setOutgoingWebRtcEndpoint(new WebRtcEndpoint.Builder(pipeline).build());
        streamSessionService.addSession(session);

        WebRtcEndpoint incomingWebRtcEndpoint = session.getOutgoingWebRtcEndpoint();
        incomingWebRtcEndpoint.addIceCandidateFoundListener(iceCandidateFoundEvent -> {
            Map<String, Object> message = Map.of(
                    "type", "ice-candidate",
                    "candidate", iceCandidateFoundEvent.getCandidate()
            );
            sendJsonMessage(session, message);
        });
    }

    public void onGetParticipantsMessage(String userId, String roomId) {
        StreamSession session = streamSessionService.getSessionByUserId(userId).orElseThrow();

        List<String> participantIds = streamSessionService.getSessionsByRoomId(roomId).stream()
                .map(StreamSession::getUserId)
                .collect(Collectors.toList());

        Map<String, Object> message = Map.of(
                "type", "participants",
                "participantIds", participantIds
        );
        sendJsonMessage(session, message);
    }

    public void onIceCandidateMessage(String userId, String targetId, String candidate, String sdpMid, int sdpMLineIndex) {
        StreamSession session = streamSessionService.getSessionByUserId(userId).orElseThrow();

        IceCandidate iceCandidate = new IceCandidate(candidate, sdpMid, sdpMLineIndex);
        // fixme

        if (userId.equals(targetId)) {
            session.getOutgoingWebRtcEndpoint().addIceCandidate(iceCandidate);
            return;
        }
    }

    public void onGetVideoMessage(String userId, String targetId) {
        StreamSession userSession = streamSessionService.getSessionByUserId(userId).orElseThrow();
        StreamSession targetSession = streamSessionService.getSessionByUserId(targetId).orElseThrow();

        WebRtcEndpoint incomingWebRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
        incomingWebRtcEndpoint.connect(targetSession.getOutgoingWebRtcEndpoint());

        incomingWebRtcEndpoint.addIceCandidateFoundListener(iceCandidateFoundEvent -> {
            Map<String, Object> message = Map.of(
                    "type", "ice-candidate",
                    "userId", targetId,
                    "candidate", iceCandidateFoundEvent.getCandidate()
            );
            sendJsonMessage(userSession, message);
        });

        userSession.getIncomingWebRtcEndpoints().put(targetId, incomingWebRtcEndpoint);

        String offer = incomingWebRtcEndpoint.generateOffer();

        incomingWebRtcEndpoint.gatherCandidates();


        Map<String, Object> message = Map.of(
                "type", "sdp-offer",
                "userId", targetId,
                "sdpOffer", offer
        );
        sendJsonMessage(userSession, message);
    }

    public void onSdpAnswerMessage(String userId, String targetId, String sdpAnswer) {
        StreamSession userSession = streamSessionService.getSessionByUserId(userId).orElseThrow();

        WebRtcEndpoint incomingEndpoint = userSession.getIncomingWebRtcEndpoints().get(targetId);

        incomingEndpoint.processAnswer(sdpAnswer);

    }

    public void onSdpOfferMessage(String userId, String targetId, String sdp) {
        StreamSession session = streamSessionService.getSessionByUserId(userId).orElseThrow();
        String sdpAnswer = "";

        if (userId.equals(targetId)) {
            sdpAnswer = session.getOutgoingWebRtcEndpoint().processOffer(sdp);
            session.getOutgoingWebRtcEndpoint().gatherCandidates();
        }


        Map<String, Object> messageObject = Map.of(
                "type", "sdp-answer",
                "userId", targetId,
                "sdpAnswer", sdpAnswer);

        sendJsonMessage(session, messageObject);
    }


    private void sendJsonMessage(StreamSession streamSession, Object message) {
        WebSocketSession webSocketSession = streamSession.getWebSocketSession();
        WebSocketUtil.sendJsonMessage(webSocketSession, message);
    }


}
