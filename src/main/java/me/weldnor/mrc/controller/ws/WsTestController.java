package me.weldnor.mrc.controller.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class WsTestController {

    @MessageMapping("/test/echo")
    @SendTo("/queue/test")
    public Object greeting(String message, @Header("simpSessionId") String sessionId) {
        String answer = String.format("[%s] %s", sessionId, message);
        log.info(answer);
        return answer;
    }
}
