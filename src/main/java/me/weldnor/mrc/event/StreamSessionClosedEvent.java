package me.weldnor.mrc.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class StreamSessionClosedEvent extends ApplicationEvent {

    private final String userId;
    private final String roomId;


    public StreamSessionClosedEvent(Object source, String userId, String roomId) {
        super(source);
        this.userId = userId;
        this.roomId = roomId;
    }
}
