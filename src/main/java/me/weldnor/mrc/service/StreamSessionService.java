package me.weldnor.mrc.service;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.pojo.StreamSession;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.weldnor.mrc.utils.WebSocketUtils.sendDebugMessage;

@Service
@Slf4j
public class StreamSessionService {
    private final List<StreamSession> sessions = new ArrayList<>();

    public List<StreamSession> getAllSessions() {
        return sessions;
    }

    public List<StreamSession> getSessionsByRoomId(long roomId) {
        return sessions.stream().filter(s -> s.getRoomId() == roomId)
                .collect(Collectors.toList());
    }

    public Optional<StreamSession> getSessionByUserId(long userId) {
        return sessions.stream().filter(s -> s.getUserId() == userId)
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
            sendDebugMessage(session.getWebSocketSession(), "leaving form server...");
        } catch (IllegalStateException exception) {
            // ignore
        }
        session.close();
        sessions.remove(session);
    }
}
