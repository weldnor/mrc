package me.weldnor.mrc.controller.ws;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

import java.io.IOException;

@Component
@Slf4j
public class MyWebSocketHandler extends TextWebSocketHandler {

    private static final Gson gson = new GsonBuilder().create();


    private final UserSessionService userSessionService;

    private final MediaPipeline pipeline;

    public MyWebSocketHandler(UserSessionService userSessionService, KurentoClient kurentoClient) {
        this.userSessionService = userSessionService;
        this.pipeline = kurentoClient.createMediaPipeline();
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);

        final UserSession user = userSessionService.getSessionByWs(session).orElse(null);

        if (user != null) {
            log.info("Incoming message from user '{} id: {}'", user.getUserId(), jsonMessage.get("id").getAsString());
        } else {
            log.info("Incoming message from new user id: {} id: ", jsonMessage.get("id").getAsString());
        }

        switch (jsonMessage.get("id").getAsString()) {
            case "joinRoom":
                joinRoom(jsonMessage, session);
                break;
            case "receiveVideoFrom":
                final long userId = jsonMessage.get("sender").getAsLong();
                final UserSession sender = userSessionService.getSessionByUserId(userId)
                        .orElseThrow();
                final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
                assert user != null;
                user.receiveVideoFrom(sender, sdpOffer);
                break;
            case "leaveRoom":
                assert user != null;
                leaveRoom(user);
                break;
            case "onIceCandidate":
                JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();
                if (user != null) {
                    IceCandidate cand = new IceCandidate(candidate.get("candidate").getAsString(),
                            candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
                    user.addCandidate(cand, jsonMessage.get("userId").getAsLong());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("afterConnectionClosed");
        var userOptional = userSessionService.getSessionByWs(session);
        userOptional.ifPresent(userSessionService::closeSession);
    }

    private void joinRoom(JsonObject params, WebSocketSession session) {
        log.info("joinRoom");
        final long roomId = params.get("roomId").getAsLong();
        final long userId = params.get("userId").getAsLong();
        log.info("PARTICIPANT {}: trying to join room {}", userId, roomId);

        UserSession user = new UserSession(userId, roomId, session, pipeline);
        //
        JsonObject newParticipantMsg = new JsonObject();
        newParticipantMsg.addProperty("id", "newParticipantArrived");
        newParticipantMsg.addProperty("userId", user.getUserId());

        var participants = userSessionService.getSessionsByRoomId(roomId);
        log.info("ROOM {}: notifying other participants of new participant {}", roomId,
                userId);

        for (var participant : participants) {
            try {
                participant.sendMessage(newParticipantMsg);
            } catch (final IOException e) {
                log.info("ROOM {}: participant {} could not be notified", roomId, participant.getUserId(), e);
            }
        }
        //
        sendParticipantIds(user);
        userSessionService.addSession(user);
    }

    @SneakyThrows
    private void sendParticipantIds(UserSession session) {
        log.info("sendParticipantIds");
        var participants = userSessionService.getSessionsByRoomId(session.getRoomId());
        final JsonArray participantsArray = new JsonArray();
        for (var participant : participants) {
            if (participant.getUserId() != session.getUserId()) {
                participantsArray.add(participant.getUserId());
            }
        }

        final JsonObject existingParticipantsMsg = new JsonObject();
        existingParticipantsMsg.addProperty("id", "existingParticipants");
        existingParticipantsMsg.add("data", participantsArray);
        log.info("PARTICIPANT {}: sending a list of {} participants", session.getUserId(),
                participantsArray.size());
        session.sendMessage(existingParticipantsMsg);
    }

    private void leaveRoom(UserSession user) throws IOException {
        log.info("leaveRoom");
        log.info("PARTICIPANT {}: Leaving room {}", user.getUserId(), user.getRoomId());
        user.close();
    }

    @Scheduled(fixedDelay = 10000)
    public void statistic() {
        var users = userSessionService.getAllSessions();
        log.info("active sessions: {}", users);
    }
}
