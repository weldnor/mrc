package me.weldnor.mrc.service;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.pojo.StreamSession;
import me.weldnor.mrc.event.StreamSessionClosedEvent;
import me.weldnor.mrc.utils.WebSocketUtil;
import org.kurento.client.*;
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
    public static final String COMPRESSED_FILTER_PARAMS = "capsfilter caps=video/x-raw,width=50,height=50,framerate=15/1";
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

        WebRtcEndpoint outgoingWebRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
        session.setOutgoingWebRtcEndpoint(outgoingWebRtcEndpoint);

        Filter outgoingCompressedFilter = new GStreamerFilter.Builder(pipeline, COMPRESSED_FILTER_PARAMS).build();
        outgoingWebRtcEndpoint.connect(outgoingCompressedFilter);
        session.setOutgoingCompressedFilter(outgoingCompressedFilter);

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

    public void onZoomMessage(String userId, String targetId, boolean enabled) {
        StreamSession userSession = streamSessionService.getSessionByUserId(userId).orElseThrow();
        StreamSession targetSession = streamSessionService.getSessionByUserId(targetId).orElseThrow();

        WebRtcEndpoint incomingEndpoint = userSession.getIncomingWebRtcEndpoints().get(targetId);

        if (enabled) {
            targetSession.getOutgoingWebRtcEndpoint().disconnect(incomingEndpoint);
            targetSession.getOutgoingCompressedFilter().connect(incomingEndpoint);
            return;
        }
        // else
        targetSession.getOutgoingCompressedFilter().disconnect(incomingEndpoint);
        targetSession.getOutgoingWebRtcEndpoint().connect(incomingEndpoint);
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
        targetSession.getOutgoingCompressedFilter().connect(incomingEndpoint);

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
