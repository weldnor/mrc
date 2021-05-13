package me.weldnor.mrc.domain.pojo;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.Continuation;
import org.kurento.client.IceCandidate;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Getter
@Setter
public class UserSession implements Closeable {
    private long userId;
    private long roomId;
    private final WebSocketSession webSocketSession;

    private final MediaPipeline pipeline;

    private final WebRtcEndpoint outgoingMedia;
    private final ConcurrentMap<Long, WebRtcEndpoint> incomingMedia = new ConcurrentHashMap<>();

    public UserSession(
            final long userId, long roomId, final WebSocketSession webSocketSession,
            MediaPipeline pipeline
    ) {

        this.pipeline = pipeline;
        this.userId = userId;
        this.roomId = roomId;
        this.webSocketSession = webSocketSession;
        this.outgoingMedia = new WebRtcEndpoint.Builder(pipeline).build();
        this.outgoingMedia.addIceCandidateFoundListener(event -> {
            JsonObject response = new JsonObject();
            response.addProperty("id", "iceCandidate");
            response.addProperty("userId", userId);
            response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
            try {
                synchronized (webSocketSession) {
                    webSocketSession.sendMessage(new TextMessage(response.toString()));
                }
            } catch (IOException e) {
                log.info(e.getMessage());
            }
        });
    }

    public void receiveVideoFrom(UserSession sender, String sdpOffer) throws IOException {
        log.info("USER {}: connecting with {} in room {}", this.userId, sender.getUserId(), this.roomId);

        log.info("USER {}: SdpOffer for {}", this.userId, sender.getUserId());

        final String ipSdpAnswer = this.getEndpointForUser(sender).processOffer(sdpOffer);
        final JsonObject scParams = new JsonObject();
        scParams.addProperty("id", "receiveVideoAnswer");
        scParams.addProperty("userId", sender.getUserId());
        scParams.addProperty("sdpAnswer", ipSdpAnswer);

        log.info("USER {}: SdpAnswer for {} is {}", this.getUserId(), sender.getUserId(), ipSdpAnswer);
        this.sendMessage(scParams);
        log.info("gather candidates");
        this.getEndpointForUser(sender).gatherCandidates();
    }

    private WebRtcEndpoint getEndpointForUser(final UserSession sender) {
        if (sender.getUserId() == userId) {
            log.info("PARTICIPANT {}: configuring loopback", this.getUserId());
            return outgoingMedia;
        }

        log.info("PARTICIPANT {}: receiving video from {}", this.getUserId(), sender.getUserId());

        WebRtcEndpoint incoming = incomingMedia.get(sender.getUserId());
        if (incoming == null) {
            log.info("PARTICIPANT {}: creating new endpoint for {}", this.getUserId(), sender.getUserId());
            incoming = new WebRtcEndpoint.Builder(pipeline).build();

            incoming.addIceCandidateFoundListener(event -> {
                JsonObject response = new JsonObject();
                response.addProperty("id", "iceCandidate");
                response.addProperty("userId", sender.getUserId());
                response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));

                log.info("addIceCandidateFoundListener");
                log.info(response.toString());

                try {
                    synchronized (webSocketSession) {
                        webSocketSession.sendMessage(new TextMessage(response.toString()));
                    }
                } catch (IOException e) {
                    log.info(e.getMessage());
                }
            });

            incomingMedia.put(sender.getUserId(), incoming);
        }

        log.info("PARTICIPANT {}: obtained endpoint for {}", this.getUserId(), sender.getUserId());
        sender.getOutgoingMedia().connect(incoming);

        return incoming;
    }

    public void cancelVideoFrom(final UserSession sender) {
        this.cancelVideoFrom(sender.getUserId());
    }

    public void cancelVideoFrom(long senderId) {
        log.info("PARTICIPANT {}: canceling video reception from {}", this.getUserId(), senderId);
        final WebRtcEndpoint incoming = incomingMedia.remove(senderId);

        log.info("PARTICIPANT {}: removing endpoint for {}", this.getUserId(), senderId);
        incoming.release(new Continuation<Void>() {
            @Override
            public void onSuccess(Void result) throws Exception {
                log.info("PARTICIPANT {}: Released successfully incoming EP for {}",
                        UserSession.this.getUserId(), senderId);
            }

            @Override
            public void onError(Throwable cause) throws Exception {
                log.warn("PARTICIPANT {}: Could not release incoming EP for {}", UserSession.this.userId,
                        senderId);
            }
        });
    }

    @Override
    public void close() throws IOException {
        log.info("PARTICIPANT {}: Releasing resources", this.getUserId());
        for (final Long remoteParticipantId : incomingMedia.keySet()) {

            log.info("PARTICIPANT {}: Released incoming EP for {}", this.userId, remoteParticipantId);

            final WebRtcEndpoint ep = this.incomingMedia.get(remoteParticipantId);

            ep.release(new Continuation<>() {

                @Override
                public void onSuccess(Void result) {
                    log.info("PARTICIPANT {}: Released successfully incoming EP for {}",
                            UserSession.this.userId, remoteParticipantId);
                }

                @Override
                public void onError(Throwable cause) {
                    log.warn("PARTICIPANT {}: Could not release incoming EP for {}", UserSession.this.userId,
                            remoteParticipantId);
                }
            });
        }

        outgoingMedia.release(new Continuation<>() {
            @Override
            public void onSuccess(Void result) {
                log.info("PARTICIPANT {}: Released outgoing EP", UserSession.this.userId);
            }

            @Override
            public void onError(Throwable cause) {
                log.warn("USER {}: Could not release outgoing EP", UserSession.this.userId);
            }
        });
    }

    public void sendMessage(JsonObject message) throws IOException {
        log.info("USER {}: Sending message with id: {}", userId, message.get("id").getAsString());
        synchronized (webSocketSession) {
            webSocketSession.sendMessage(new TextMessage(message.toString()));
        }
    }

    public void addCandidate(IceCandidate candidate, long userId) {
        if (this.userId == userId) {
            outgoingMedia.addIceCandidate(candidate);
        } else {
            WebRtcEndpoint webRtc = incomingMedia.get(userId);
            if (webRtc != null) {
                webRtc.addIceCandidate(candidate);
            }
        }
    }


    @Override
    public String toString() {
        return "UserSession{" +
                "userId=" + userId +
                ", roomId=" + roomId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSession that = (UserSession) o;
        return userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}

