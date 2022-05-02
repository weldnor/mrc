package me.weldnor.mrc.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public final class WebSocketUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private WebSocketUtil() {
    }


    public static void sendJsonMessage(WebSocketSession webSocketSession, Object o) {
        try {
            String message = MAPPER.writeValueAsString(o);
            sendMessage(webSocketSession, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendMessage(WebSocketSession webSocketSession, String message) {
        try {
            // fixme
            synchronized (webSocketSession) {
                webSocketSession.sendMessage(new TextMessage(message));
            }
        } catch (Exception e) {
            log.error("cant send message", e);
        }
    }

}
