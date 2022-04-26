package me.weldnor.mrc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.pojo.StreamSession;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StreamService {
    private final StreamSessionService streamSessionService;

    private final ObjectMapper mapper = new ObjectMapper();

    public StreamService(StreamSessionService streamSessionService) {
        this.streamSessionService = streamSessionService;
    }

    public void onJoinMessage(String userId, String roomId, WebSocketSession webSocketSession) {
        //todo validate

        StreamSession streamSession = new StreamSession();
        streamSession.setUserId(userId);
        streamSession.setRoomId(roomId);
        streamSession.setWebSocketSession(webSocketSession);

        streamSessionService.addSession(streamSession);
    }

    public void onGetParticipantsMessage(String userId, String roomId) {
        StreamSession session = streamSessionService.getSessionByUserId(userId).orElseThrow();

        List<String> participantIds = streamSessionService.getSessionsByRoomId(roomId).stream()
                .map(StreamSession::getUserId)
                .collect(Collectors.toList());

        Map<String, Object> messageObject = Map.of("type", "participants", "participantIds", participantIds);
        sendJsonMessage(session, messageObject);
    }


    private void sendJsonMessage(StreamSession streamSession, Object o) {
        try {
            String message = mapper.writeValueAsString(o);

            WebSocketSession webSocketSession = streamSession.getWebSocketSession();
            webSocketSession.sendMessage(new TextMessage(message));
        } catch (Exception e) {
            log.error("cant send message", e);
        }
    }
}
