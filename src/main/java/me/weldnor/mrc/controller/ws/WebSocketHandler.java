package me.weldnor.mrc.controller.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.service.StreamService;
import me.weldnor.mrc.service.StreamSessionService;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Profile("!test")
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private final StreamService streamService;

    private final StreamSessionService streamSessionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketHandler(StreamService streamService, StreamSessionService streamSessionService1) {
        this.streamService = streamService;
        this.streamSessionService = streamSessionService1;
    }

    @Override
    public void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws JsonProcessingException {
        final JsonNode parsedMessage = objectMapper.readTree(message.getPayload());

        log.info(String.valueOf(parsedMessage));

        String userId = parsedMessage.get("userId").asText();
        String roomId = parsedMessage.get("roomId").asText();
        String type = parsedMessage.get("type").asText();

        if (type.equals("join")) {
            streamService.onJoinMessage(userId, roomId, session);
            return;
        }

        String targetId = parsedMessage.get("targetId").asText();

        if (type.equals("ice-candidate") && parsedMessage.has("candidate")) {
            String candidate = parsedMessage.get("candidate").asText();
            String sdpMid = parsedMessage.get("sdpMid").asText();
            int sdpMLineIndex = parsedMessage.get("sdpMLineIndex").asInt();
            streamService.onIceCandidateMessage(userId, targetId, candidate, sdpMid, sdpMLineIndex);
            return;
        }

        if (type.equals("get-video")) {
            String sdpOffer = parsedMessage.get("sdpOffer").asText();
            streamService.onGetVideoMessage(userId, targetId, sdpOffer);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        streamSessionService.afterConnectionClosed(session, status);
    }
}
