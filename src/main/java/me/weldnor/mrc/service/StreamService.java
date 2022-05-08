package me.weldnor.mrc.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.pojo.StreamSession;
import me.weldnor.mrc.event.StreamSessionClosedEvent;
import me.weldnor.mrc.utils.WebSocketUtil;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Profile("!test")
public class StreamService {
    private final StreamSessionService streamSessionService;

    private final MediaPipeline pipeline;

    public StreamService(StreamSessionService streamSessionService, KurentoClient kurentoClient) {
        this.streamSessionService = streamSessionService;
        this.pipeline = kurentoClient.createMediaPipeline();
    }

    public void onJoinMessage(String userId, String roomId, WebSocketSession webSocketSession) {
        // 1. save session
        StreamSession session = new StreamSession();
        session.setUserId(userId);
        session.setRoomId(roomId);
        session.setWebSocketSession(webSocketSession);
        session.setOutgoingWebRtcEndpoint(new WebRtcEndpoint.Builder(pipeline).build());
        streamSessionService.addSession(session);

        // 2. add incoming endpoint
        WebRtcEndpoint incomingWebRtcEndpoint = session.getOutgoingWebRtcEndpoint();
        incomingWebRtcEndpoint.addIceCandidateFoundListener(iceCandidateFoundEvent -> {
            Map<String, Object> message = Map.of("type", "ice-candidate", "userId", userId, "candidate", iceCandidateFoundEvent.getCandidate());
            sendJsonMessage(session, message);
        });

        // 3. send participants ids
        sendParticipantIds(userId, roomId);
        notifyThatParticipantJoin(userId, roomId);
    }

    public void onIceCandidateMessage(String userId, String targetId, String candidate, String sdpMid, int sdpMLineIndex) {
        IceCandidate iceCandidate = new IceCandidate(candidate, sdpMid, sdpMLineIndex);

        getEndpointForUser(userId, targetId).addIceCandidate(iceCandidate);
    }

    @EventListener(StreamSessionClosedEvent.class)
    public void onSessionStreamClosed(StreamSessionClosedEvent event) {
        notifyThatParticipantLeft(event.getUserId(), event.getRoomId());
    }

    public void onGetVideoMessage(String userId, String targetId, String sdpOffer) {
        StreamSession userSession = streamSessionService.getSessionByUserId(userId).orElseThrow();

        WebRtcEndpoint endpoint = getEndpointForUser(userId, targetId);

        String sdpAnswer = endpoint.processOffer(sdpOffer);

        // send sdp answer
        Map<String, Object> message = Map.of(
                "type", "sdp-answer",
                "userId", targetId,
                "sdpAnswer", sdpAnswer
        );
        sendJsonMessage(userSession, message);

        // gather candidates
        endpoint.gatherCandidates();
    }

    public void onRequestControlMessage(String userId, String targetId) {
    }

    public void onAcceptControlMessage(String userId, String targetId) {
    }

    public void onControlMessage(String userId, String targetId, JsonNode command) {
        StreamSession targetSession = streamSessionService.getSessionByUserId(targetId).orElseThrow();

        Map<String, Object> message = Map.of(
                "type", "control",
                "userId", userId,
                "command", command
        );
        sendJsonMessage(targetSession, message);
    }


    public WebRtcEndpoint getEndpointForUser(String userId, String targetId) {
        StreamSession userSession = streamSessionService.getSessionByUserId(userId).orElseThrow();
        StreamSession targetSession = streamSessionService.getSessionByUserId(targetId).orElseThrow();

        if (userId.equals(targetId)) {
            return userSession.getOutgoingWebRtcEndpoint();
        }

        WebRtcEndpoint incomingEndpoint = userSession.getIncomingWebRtcEndpoints().get(targetId);
        if (incomingEndpoint != null) {
            return incomingEndpoint;
        }

        // create new endpoint
        incomingEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
        targetSession.getOutgoingWebRtcEndpoint().connect(incomingEndpoint);

        incomingEndpoint.addIceCandidateFoundListener(iceCandidateFoundEvent -> {
            Map<String, Object> message = Map.of(
                    "type", "ice-candidate",
                    "userId", targetId,
                    "candidate", iceCandidateFoundEvent.getCandidate()
            );
            sendJsonMessage(userSession, message);
        });

        userSession.getIncomingWebRtcEndpoints().put(targetId, incomingEndpoint);
        return incomingEndpoint;
    }

    private void sendParticipantIds(String userId, String roomId) {
        StreamSession session = streamSessionService.getSessionByUserId(userId).orElseThrow();

        List<String> participantIds = streamSessionService.getSessionsByRoomId(roomId).stream()
                .map(StreamSession::getUserId)
                .filter(participantId -> !participantId.equals(userId))
                .collect(Collectors.toList());

        Map<String, Object> message = Map.of("type", "participants", "participantIds", participantIds);
        sendJsonMessage(session, message);
    }

    private void notifyThatParticipantLeft(String userId, String roomId) {
        Map<String, Object> message = Map.of(
                "type", "participants/left",
                "userId", userId
        );

        streamSessionService.getSessionsByRoomId(roomId)
                .forEach(session -> sendJsonMessage(session, message));
    }

    private void notifyThatParticipantJoin(String userId, String roomId) {
        Map<String, Object> message = Map.of(
                "type", "participants/new",
                "userId", userId
        );

        streamSessionService.getSessionsByRoomId(roomId).stream()
                .filter(session -> !session.getUserId().equals(userId))
                .forEach(session -> sendJsonMessage(session, message));
    }

    private void sendJsonMessage(StreamSession streamSession, Object message) {
        WebSocketSession webSocketSession = streamSession.getWebSocketSession();
        WebSocketUtil.sendJsonMessage(webSocketSession, message);
    }

}
