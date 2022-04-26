package me.weldnor.mrc.controller.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.weldnor.mrc.service.StreamService;
import me.weldnor.mrc.service.StreamSessionService;
import org.kurento.client.KurentoClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Profile("!test")
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private final StreamService streamService;

    private final StreamSessionService streamSessionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketHandler(StreamSessionService streamSessionService, KurentoClient kurentoClient, StreamService streamService, StreamSessionService streamSessionService1) {
        this.streamService = streamService;
        this.streamSessionService = streamSessionService1;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws JsonProcessingException {
        final JsonNode parsedMessage = objectMapper.readTree(message.getPayload());

        log.info(String.valueOf(parsedMessage));


        switch (parsedMessage.get("type").asText()) {
            case "join": {
                String userId = parsedMessage.get("userId").asText();
                String roomId = parsedMessage.get("roomId").asText();
                streamService.onJoinMessage(userId, roomId, session);
                break;
            }
            case "get-participants": {
                String userId = parsedMessage.get("userId").asText();
                String roomId = parsedMessage.get("roomId").asText();
                streamService.onGetParticipantsMessage(userId, roomId);
                break;
            }

//            case "leaveRoom":
//                assert user != null;
//                onLeaveRoom(user);
//                break;
//            case "onIceCandidate":
//                JsonNode candidate = parsedMessage.get("candidate");
//                if (user != null) {
//                    IceCandidate cand = new IceCandidate(candidate.get("candidate").asText(),
//                            candidate.get("sdpMid").asText(), candidate.get("sdpMLineIndex").asInt());
//                    user.addCandidate(cand, parsedMessage.get("userId").asLong());
//                }
//                break;
//            case "ping":
//                assert user != null;
//                onPing(user);
//                break;
//            case "acceptFilter":
//                assert user != null;
//                onAcceptFilter(user, parsedMessage.get("targetId").asLong());
//                break;
//            case "declineFilter":
//                assert user != null;
//                onDeclineFilter(user, parsedMessage.get("targetId").asLong());
//                break;
            default:
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        streamSessionService.afterConnectionClosed(session, status);

    }

    //    private void onAcceptFilter(StreamSession user, long targetId) {
//        log.info("onAcceptFilter");
//        var target = streamSessionService.getSessionByUserId(targetId).orElseThrow();
//        user.acceptFilterForUser(target);
//    }
//
//    private void onDeclineFilter(StreamSession user, long targetId) {
//        log.info("onDeclineFilter");
//        var target = streamSessionService.getSessionByUserId(targetId).orElseThrow();
//        user.declineFilterForUser(target);
//    }

//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//        log.info("afterConnectionClosed");
//        streamSessionService.getSessionByWs(session)
//                .ifPresent(this::onLeaveRoom);
//    }
//
//    private void onJoinRoom(WebSocketSession session, long userId, long roomId) {
//        log.info("joinRoom");
//        log.info("PARTICIPANT {}: trying to join room {}", userId, roomId);
//
//        StreamSession user = new StreamSession(userId, roomId, session, pipeline);
//
//        notifyThatUserJoinToRoom(user);
//        sendParticipantIds(user);
//
//        streamSessionService.addSession(user);
//    }
//
//    private void notifyThatUserJoinToRoom(StreamSession user) {
//        var userId = user.getUserId();
//        var roomId = user.getRoomId();
//        ObjectNode newParticipantMsg = objectMapper.createObjectNode();
//        newParticipantMsg.put("id", "newParticipantArrived");
//        newParticipantMsg.put("userId", user.getUserId());
//
//        var participants = streamSessionService.getSessionsByRoomId(roomId);
//        log.info("ROOM {}: notifying other participants of new participant {}", roomId,
//                userId);
//
//        for (var participant : participants) {
//            participant.sendMessage(newParticipantMsg);
//        }
//    }
//
//    private void notifyThatUserLeaveRoom(StreamSession user) {
//        var userId = user.getUserId();
//        var roomId = user.getRoomId();
//
//        ObjectNode newParticipantMsg = objectMapper.createObjectNode();
//        newParticipantMsg.put("id", "participantLeft");
//        newParticipantMsg.put("userId", user.getUserId());
//
//        var participants = streamSessionService.getSessionsByRoomId(roomId);
//        log.info("ROOM {}: notifying other participants of left participant {}", roomId,
//                userId);
//
//        for (var participant : participants) {
//            if (!participant.equals(user)) {
//                participant.sendMessage(newParticipantMsg);
//            }
//        }
//    }
//
//    @SneakyThrows
//    private void sendParticipantIds(StreamSession session) {
//        log.info("sendParticipantIds");
//        var participants = streamSessionService.getSessionsByRoomId(session.getRoomId());
//
//        ArrayNode participantsArray = objectMapper.createArrayNode();
//        for (var participant : participants) {
//            if (participant.getUserId() != session.getUserId()) {
//                participantsArray.add(participant.getUserId());
//            }
//        }
//
//        ObjectNode existingParticipantsMsg = objectMapper.createObjectNode();
//        existingParticipantsMsg.put("id", "existingParticipants");
//        existingParticipantsMsg.set("data", participantsArray);
//
//        log.info("PARTICIPANT {}: sending a list of {} participants", session.getUserId(),
//                participantsArray.size());
//
//        session.sendMessage(existingParticipantsMsg);
//    }
//
//    private void onLeaveRoom(StreamSession user) {
//        log.info("leaveRoom");
//        log.info("PARTICIPANT {}: Leaving room {}", user.getUserId(), user.getRoomId());
//        notifyThatUserLeaveRoom(user);
//        streamSessionService.closeSession(user);
//    }

//    @Scheduled(fixedDelay = 15000)
//    public void statistic() {
//        var users = streamSessionService.getAllSessions();
//        log.info("active sessions: {}", users);
//    }
}
