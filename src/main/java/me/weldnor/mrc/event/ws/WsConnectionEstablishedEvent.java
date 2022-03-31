package me.weldnor.mrc.event.ws;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class WsConnectionEstablishedEvent extends ApplicationEvent {

    private final WebSocketSession webSocketSession;

    public WsConnectionEstablishedEvent(Object source, WebSocketSession webSocketSession) {
        super(source);
        this.webSocketSession = webSocketSession;
    }
}
