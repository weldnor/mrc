package me.weldnor.mrc.event.ws;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class WsMessageReceivedEvent extends ApplicationEvent {

    private final WebSocketSession webSocketSession;
    private final TextMessage message;

    public WsMessageReceivedEvent(Object source, WebSocketSession webSocketSession, TextMessage message) {
        super(source);
        this.webSocketSession = webSocketSession;
        this.message = message;
    }
}
