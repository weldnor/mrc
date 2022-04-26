package me.weldnor.mrc.domain.pojo;

import lombok.Data;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.springframework.web.socket.WebSocketSession;

@Data
public class StreamSession {

    private String userId;
    private String roomId;

    @EqualsExclude
    @ToStringExclude
    private WebSocketSession webSocketSession;
}

