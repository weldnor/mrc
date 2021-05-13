package me.weldnor.mrc.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class WebSocketUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public static void sendMessage(WebSocketSession session, String message) {
        log.info("sending message: " + message);
        session.sendMessage(new TextMessage(message));
    }

    @SneakyThrows
    public static void sendDebugMessage(WebSocketSession session, String message) {
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.set("type", new TextNode("debug"));
        jsonNode.set("text", new TextNode(message));
        sendMessage(session, jsonNode.toPrettyString());
    }
}
