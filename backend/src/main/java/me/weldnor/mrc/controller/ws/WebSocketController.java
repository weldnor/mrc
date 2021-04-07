package me.weldnor.mrc.controller.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    private final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @MessageMapping("/message")
    @SendTo("/topic/message")
    public String sendMessage(String message) {
        logger.info(message);
        return "received message: " + message;
    }
}
