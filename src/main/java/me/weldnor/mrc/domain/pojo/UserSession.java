package me.weldnor.mrc.domain.pojo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.utils.WebSocketUtils;
import org.kurento.client.*;
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

    private final WebRtcEndpoint outgoingFullMedia;
    private final Filter compressionFilter;
    private final ConcurrentMap<Long, WebRtcEndpoint> incomingMedia = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserSession(final long userId, long roomId, final WebSocketSession webSocketSession, MediaPipeline pipeline) {
        this.pipeline = pipeline;
        this.userId = userId;
        this.roomId = roomId;
        this.webSocketSession = webSocketSession;

        this.outgoingFullMedia = new WebRtcEndpoint.Builder(pipeline).build();
        this.outgoingFullMedia.addIceCandidateFoundListener(event -> iceCandidateFound(event.getCandidate()));

        compressionFilter = new GStreamerFilter.Builder(pipeline, "capsfilter caps=video/x-raw,width=50,height=50,framerate=15/1").build();
        outgoingFullMedia.connect(compressionFilter);
//        compressionFilter.connect(outgoingFullMedia);
    }

    /**
     * @param candidate IceCandidate текущего пользователя
     */
    @SneakyThrows
    private void iceCandidateFound(IceCandidate candidate) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("id", "iceCandidate");
        response.put("userId", userId);
        response.set("candidate", objectMapper.valueToTree(candidate));

        synchronized (webSocketSession) {
            this.sendMessage(response);
        }
    }

    /**
     * @param sender   - сессия пользователя,видео которого мы хотим получить
     * @param sdpOffer - передаваемый sdpOffer текущего пользователя
     */
    public void receiveVideoFrom(UserSession sender, String sdpOffer) {
        long senderId = sender.getUserId();
        log.info("USER {}: connecting with {} in room {}", this.userId, senderId, this.roomId);
        log.info("USER {}: SdpOffer for {}", this.userId, senderId);

        final String ipSdpAnswer = this.getEndpointForUser(sender).processOffer(sdpOffer);
        final ObjectNode scParams = objectMapper.createObjectNode();
        scParams.put("id", "receiveVideoAnswer");
        scParams.put("userId", senderId);
        scParams.put("sdpAnswer", ipSdpAnswer);

        log.info("USER {}: SdpAnswer for {} is {}", this.userId, senderId, ipSdpAnswer);
        this.sendMessage(scParams);
        log.info("gather candidates");
        this.getEndpointForUser(sender).gatherCandidates();
    }

    /**
     * Получение или создание WebRtcEndpoint для получения видео пользователя.
     *
     * @param sender сессия пользователя
     * @return WebRtcEndpoint для подключения к пользователю
     */
    private WebRtcEndpoint getEndpointForUser(final UserSession sender) {
        long senderId = sender.getUserId();

        if (senderId == this.userId) {
            log.info("PARTICIPANT {}: configuring loopback", this.getUserId());
            return outgoingFullMedia;
        }

        log.info("PARTICIPANT {}: receiving video from {}", this.getUserId(), senderId);

        WebRtcEndpoint incoming = incomingMedia.get(senderId);

        if (incoming == null) {
            incoming = createEndpointForUser(sender, false);
        }
        return incoming;
    }

    /**
     * Создание  создание WebRtcEndpoint для получения видео пользователя.
     *
     * @param sender сессия пользователя, с которым надо создать соединение
     * @return WebRtcEndpoint для подключения к пользователю
     */
    private WebRtcEndpoint createEndpointForUser(UserSession sender, boolean full) {
        long senderId = sender.getUserId();
        log.info("PARTICIPANT {}: creating new endpoint for {}", this.getUserId(), senderId);

        var incoming = new WebRtcEndpoint.Builder(pipeline).build();

        incoming.addIceCandidateFoundListener(event -> {
            JsonObject response = new JsonObject();
            response.addProperty("id", "iceCandidate");
            response.addProperty("userId", senderId);
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
        incomingMedia.put(senderId, incoming);

        log.info("PARTICIPANT {}: obtained endpoint for {}", this.getUserId(), senderId);
//        if (full) {
        sender.getOutgoingFullMedia().connect(incoming);
//        } else {
//            sender.getCompressionFilter().connect(incoming);
//        }

        return incoming;
    }

    /**
     * @param senderId - id пользователя, с которым надо закрыть соединение
     */
    public void cancelVideoFrom(long senderId) {
        log.info("PARTICIPANT {}: canceling video reception from {}", this.getUserId(), senderId);
        final WebRtcEndpoint incoming = incomingMedia.remove(senderId);

        log.info("PARTICIPANT {}: removing endpoint for {}", this.getUserId(), senderId);
        incoming.release(new Continuation<>() {
            @Override
            public void onSuccess(Void result) {
                log.info("PARTICIPANT {}: Released successfully incoming EP for {}",
                        UserSession.this.getUserId(), senderId);
            }

            @Override
            public void onError(Throwable cause) {
                log.warn("PARTICIPANT {}: Could not release incoming EP for {}", UserSession.this.userId,
                        senderId);
            }
        });
    }

    /**
     * удаление сессии пользователя.
     */
    @Override
    @SneakyThrows
    public void close() {
        log.info("PARTICIPANT {}: Releasing resources", this.getUserId());

        for (final Long remoteParticipantId : incomingMedia.keySet()) {
            log.info("PARTICIPANT {}: Released incoming EP for {}", this.userId, remoteParticipantId);

            final WebRtcEndpoint ep = this.incomingMedia.get(remoteParticipantId);

            // TODO delete?
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

        outgoingFullMedia.release(new Continuation<>() {
            @Override
            public void onSuccess(Void result) {
                log.info("PARTICIPANT {}: Released outgoing EP", UserSession.this.userId);
            }

            @Override
            public void onError(Throwable cause) {
                log.warn("USER {}: Could not release outgoing EP", UserSession.this.userId);
            }
        });

        webSocketSession.close();
    }

    /**
     * Добавление IceCandidate для подключения пользователя к своим Endpoint.
     *
     * @param candidate - IceCandidate
     * @param senderId  - id пользователя
     */
    public void addCandidate(IceCandidate candidate, long senderId) {
        if (this.userId == senderId) {
            outgoingFullMedia.addIceCandidate(candidate);
        } else {
            WebRtcEndpoint webRtc = incomingMedia.get(senderId);
            if (webRtc != null) {
                webRtc.addIceCandidate(candidate);
            }
        }
    }

    /**
     * @param message - отправляемое пользователю сообщение
     */
    public void sendDebugMessage(String message) {
        log.debug("USER {}: Sending debug message: {}", userId, message);
        synchronized (webSocketSession) {
            WebSocketUtils.sendDebugMessage(webSocketSession, message);
        }
    }

    /**
     * @param message - отправляемое пользователю сообщение
     */
    public void sendMessage(JsonNode message) {
        log.debug("USER {}: Sending message with id: {}", userId, message.get("id").asText());
        synchronized (webSocketSession) {
            WebSocketUtils.sendMessage(webSocketSession, message);
        }
    }

    /**
     * @param message - отправляемое пользователю сообщение
     */
    public void sendMessage(String message) {
        log.debug("USER {}: Sending message {}", userId, message);
        synchronized (webSocketSession) {
            WebSocketUtils.sendMessage(webSocketSession, message);
        }
    }

    @Override
    public String toString() {
        return "UserSession{"
                + "userId=" + userId
                + ", roomId=" + roomId
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserSession that = (UserSession) o;
        return userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    public void acceptFilterForUser(UserSession target) {
        var endpoint = incomingMedia.get(target.getUserId());
        target.getOutgoingFullMedia().disconnect(endpoint);
        target.getCompressionFilter().connect(endpoint);
//        endpoint.connect(target.compressionFilter);
        //            sender.getCompressionFilter().connect(incoming);
    }

    public void declineFilterForUser(UserSession target) {
        var endpoint = incomingMedia.get(target.getUserId());
        target.getCompressionFilter().disconnect(endpoint);
        target.getOutgoingFullMedia().connect(endpoint);
    }
}

