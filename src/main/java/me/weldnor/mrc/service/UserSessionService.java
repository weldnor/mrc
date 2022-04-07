package me.weldnor.mrc.service;

import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.pojo.UserSession;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static me.weldnor.mrc.utils.WebSocketUtils.sendDebugMessage;

@Service
@Slf4j
public class UserSessionService {
    private final List<UserSession> sessions = new ArrayList<>();

    public List<UserSession> getAllSessions() {
        return sessions;
    }

    public List<UserSession> getSessionsByRoomId(long roomId) {
        return sessions.stream().filter(s -> s.getRoomId() == roomId)
                .collect(Collectors.toList());
    }

    public Optional<UserSession> getSessionByUserId(long userId) {
        return sessions.stream().filter(s -> s.getUserId() == userId)
                .findFirst();
    }

    public Optional<UserSession> getSessionByWs(WebSocketSession webSocketSession) {
        return sessions.stream().filter(s -> s.getWebSocketSession().equals(webSocketSession))
                .findFirst();
    }

    public void addSession(UserSession userSession) {
        log.info("add session with user id: " + userSession.getUserId());
        sessions.add(userSession);
    }

    public void closeSession(UserSession session) {
        try {
            sendDebugMessage(session.getWebSocketSession(), "leaving form server...");
        } catch (IllegalStateException exception) {
            // ignore
        }
        session.close();
        sessions.remove(session);
    }
}
