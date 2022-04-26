package me.weldnor.mrc.service;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.pojo.StreamSession;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class StreamSessionService {
    private final List<StreamSession> sessions = new ArrayList<>();

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

    public void closeSession(StreamSession session) {
        try {
            session.getWebSocketSession().close();
        } catch (IOException e) {
            // ignore
        }
        sessions.remove(session);
    }

    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus status) {
        Optional<StreamSession> streamSessionOptional = getSessionByWs(webSocketSession);
        streamSessionOptional.ifPresent(this::closeSession);
    }
}
