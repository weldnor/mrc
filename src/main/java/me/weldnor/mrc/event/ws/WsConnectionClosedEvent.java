package me.weldnor.mrc.event.ws;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

@Getter
public class WsConnectionClosedEvent extends ApplicationEvent {

    private final WebSocketSession webSocketSession;
    private final CloseStatus status;

    public WsConnectionClosedEvent(Object source, WebSocketSession webSocketSession, CloseStatus status) {
        super(source);
        this.webSocketSession = webSocketSession;
        this.status = status;
    }
}
