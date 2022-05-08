package me.weldnor.mrc.service;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.pojo.StreamSession;
import me.weldnor.mrc.event.StreamSessionClosedEvent;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;


@Service
@Slf4j
public class StreamSessionService {

    private final ApplicationEventPublisher eventPublisher;
    private final List<StreamSession> sessions = new ArrayList<>();

    public StreamSessionService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public List<StreamSession> getAllSessions() {
        return sessions;
    }

    public List<StreamSession> getSessionsByRoomId(String roomId) {
        return sessions.stream().filter(s -> s.getRoomId().equals(roomId))
                .collect(Collectors.toList());
    }

    public Optional<StreamSession> getSessionByUserId(String userId) {
        return sessions.stream().filter(s -> s.getUserId().equals(userId))
                .findFirst();
    }

    public Optional<StreamSession> getSessionByWs(WebSocketSession webSocketSession) {
        return sessions.stream().filter(s -> s.getWebSocketSession().equals(webSocketSession))
                .findFirst();
    }

    public void addSession(StreamSession streamSession) {
        log.info("add session with user id: " + streamSession.getUserId());
        sessions.add(streamSession);
    }

    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus status) {
        Optional<StreamSession> streamSessionOptional = getSessionByWs(webSocketSession);

        if (streamSessionOptional.isEmpty()) {
            return;
        }

        StreamSession streamSession = streamSessionOptional.get();
        String userId = streamSession.getUserId();
        String roomId = streamSession.getRoomId();

        log.info("closed session with user id: " + streamSession.getUserId());
        closeSession(streamSession);

        StreamSessionClosedEvent event = new StreamSessionClosedEvent(this, userId, roomId);
        eventPublisher.publishEvent(event);
    }

    public void closeSession(StreamSession session) {
        String userId = session.getUserId();

        // release connections in removed user
        for (WebRtcEndpoint endpoint : session.getIncomingWebRtcEndpoints().values()) {
            endpoint.release();
        }
        session.getOutgoingWebRtcEndpoint().release();

        // release connections in other users
        for (StreamSession streamSession : getAllSessions()) {
            if (streamSession == session) {
                continue;
            }

            ConcurrentMap<String, WebRtcEndpoint> incomingWebRtcEndpoints = streamSession.getIncomingWebRtcEndpoints();

            if (incomingWebRtcEndpoints.containsKey(userId)) {
                incomingWebRtcEndpoints.get(userId).release();
                incomingWebRtcEndpoints.remove(userId);
            }
        }

        // release websocket connection
        try {
            session.getWebSocketSession().close();
        } catch (IOException e) {
            // ignore
        }

        sessions.remove(session);
    }
}
