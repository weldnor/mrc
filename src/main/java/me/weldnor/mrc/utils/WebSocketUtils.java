package me.weldnor.mrc.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
public final class WebSocketUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private WebSocketUtils() {
    }

    public static void sendMessage(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            log.warn("cant send message, because socket is closed");
        }
    }

    public static void sendMessage(WebSocketSession session, JsonNode message) {
        sendMessage(session, message.toPrettyString());
    }

    public static void sendDebugMessage(WebSocketSession session, String message) {
        ObjectNode jsonNode = OBJECT_MAPPER.createObjectNode();
        jsonNode.set("type", new TextNode("debug"));
        jsonNode.set("text", new TextNode(message));
        sendMessage(session, jsonNode.toPrettyString());
    }
}
