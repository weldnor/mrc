package me.weldnor.mrc.controller.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.domain.pojo.UserSession;
import me.weldnor.mrc.service.UserSessionService;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private final MediaPipeline pipeline;

    private final UserSessionService userSessionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketHandler(UserSessionService userSessionService, KurentoClient kurentoClient) {
        this.userSessionService = userSessionService;
        this.pipeline = kurentoClient.createMediaPipeline();
    }


    @Override
    @SneakyThrows
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        final JsonNode parsedMessage = objectMapper.readTree(message.getPayload());

        final UserSession user = userSessionService.getSessionByWs(session).orElse(null);

        if (user != null) {
            log.info("Incoming message from user '{} id: {}'", user.getUserId(), parsedMessage.get("id").asText());
        } else {
            log.info("Incoming message from new user id: {} id: ", parsedMessage.get("id").asText());
        }

        switch (parsedMessage.get("id").asText()) {
            case "joinRoom":
                long userId = parsedMessage.get("userId").asLong();
                final long roomId = parsedMessage.get("roomId").asLong();
                onJoinRoom(session, userId, roomId);
                break;
            case "receiveVideoFrom":
                userId = parsedMessage.get("userId").asLong();
                final UserSession sender = userSessionService.getSessionByUserId(userId)
                        .orElseThrow();
                final String sdpOffer = parsedMessage.get("sdpOffer").asText();
                assert user != null;
                user.receiveVideoFrom(sender, sdpOffer);
                break;
            case "leaveRoom":
                assert user != null;
                onLeaveRoom(user);
                break;
            case "onIceCandidate":
                JsonNode candidate = parsedMessage.get("candidate");
                if (user != null) {
                    IceCandidate cand = new IceCandidate(candidate.get("candidate").asText(),
                            candidate.get("sdpMid").asText(), candidate.get("sdpMLineIndex").asInt());
                    user.addCandidate(cand, parsedMessage.get("userId").asLong());
                }
                break;
            case "ping":
                assert user != null;
                onPing(user);
                break;
            case "acceptFilter":
                assert user != null;
                onAcceptFilter(user, parsedMessage.get("targetId").asLong());
                break;
            case "declineFilter":
                assert user != null;
                onDeclineFilter(user, parsedMessage.get("targetId").asLong());
                break;
            default:
                break;
        }
    }

    private void onAcceptFilter(UserSession user, long targetId) {
        log.info("onAcceptFilter");
        var target = userSessionService.getSessionByUserId(targetId).orElseThrow();
        user.acceptFilterForUser(target);
    }

    private void onDeclineFilter(UserSession user, long targetId) {
        log.info("onDeclineFilter");
        var target = userSessionService.getSessionByUserId(targetId).orElseThrow();
        user.declineFilterForUser(target);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("afterConnectionClosed");
        userSessionService.getSessionByWs(session)
                .ifPresent(this::onLeaveRoom);
    }

    private void onJoinRoom(WebSocketSession session, long userId, long roomId) {
        log.info("joinRoom");
        log.info("PARTICIPANT {}: trying to join room {}", userId, roomId);

        UserSession user = new UserSession(userId, roomId, session, pipeline);

        notifyThatUserJoinToRoom(user);
        sendParticipantIds(user);

        userSessionService.addSession(user);
    }

    private void notifyThatUserJoinToRoom(UserSession user) {
        var userId = user.getUserId();
        var roomId = user.getRoomId();
        ObjectNode newParticipantMsg = objectMapper.createObjectNode();
        newParticipantMsg.put("id", "newParticipantArrived");
        newParticipantMsg.put("userId", user.getUserId());

        var participants = userSessionService.getSessionsByRoomId(roomId);
        log.info("ROOM {}: notifying other participants of new participant {}", roomId,
                userId);

        for (var participant : participants) {
            participant.sendMessage(newParticipantMsg);
        }
    }

    private void notifyThatUserLeaveRoom(UserSession user) {
        var userId = user.getUserId();
        var roomId = user.getRoomId();

        ObjectNode newParticipantMsg = objectMapper.createObjectNode();
        newParticipantMsg.put("id", "participantLeft");
        newParticipantMsg.put("userId", user.getUserId());

        var participants = userSessionService.getSessionsByRoomId(roomId);
        log.info("ROOM {}: notifying other participants of left participant {}", roomId,
                userId);

        for (var participant : participants) {
            if (!participant.equals(user)) {
                participant.sendMessage(newParticipantMsg);
            }
        }
    }

    @SneakyThrows
    private void sendParticipantIds(UserSession session) {
        log.info("sendParticipantIds");
        var participants = userSessionService.getSessionsByRoomId(session.getRoomId());

        ArrayNode participantsArray = objectMapper.createArrayNode();
        for (var participant : participants) {
            if (participant.getUserId() != session.getUserId()) {
                participantsArray.add(participant.getUserId());
            }
        }

        ObjectNode existingParticipantsMsg = objectMapper.createObjectNode();
        existingParticipantsMsg.put("id", "existingParticipants");
        existingParticipantsMsg.set("data", participantsArray);

        log.info("PARTICIPANT {}: sending a list of {} participants", session.getUserId(),
                participantsArray.size());

        session.sendMessage(existingParticipantsMsg);
    }

    private void onLeaveRoom(UserSession user) {
        log.info("leaveRoom");
        log.info("PARTICIPANT {}: Leaving room {}", user.getUserId(), user.getRoomId());
        notifyThatUserLeaveRoom(user);
        userSessionService.closeSession(user);
    }

    private void onPing(UserSession user) {
        var json = objectMapper.createObjectNode();
        json.put("id", "pong");
        user.sendMessage(json);
    }

    @Scheduled(fixedDelay = 15000)
    public void statistic() {
        var users = userSessionService.getAllSessions();
        log.info("active sessions: {}", users);
    }
}