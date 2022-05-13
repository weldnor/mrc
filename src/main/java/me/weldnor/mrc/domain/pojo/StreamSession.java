package me.weldnor.mrc.domain.pojo;

import lombok.Data;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Data
public class StreamSession {

    private String userId;
    private String roomId;
    private Set<String> trustedUserIds = new HashSet<>();

    @EqualsExclude
    @ToStringExclude
    private final ConcurrentMap<String, WebRtcEndpoint> incomingWebRtcEndpoints = new ConcurrentHashMap<>();
    @EqualsExclude
    @ToStringExclude
    private WebRtcEndpoint outgoingWebRtcEndpoint;

    @EqualsExclude
    @ToStringExclude
    private WebSocketSession webSocketSession;
}

