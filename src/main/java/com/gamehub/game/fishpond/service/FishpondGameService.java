package com.gamehub.game.fishpond.service;

import com.gamehub.domain.game.GameStatus;
import com.gamehub.domain.room.GameRoom;
import com.gamehub.domain.room.RoomPlayer;
import com.gamehub.domain.room.RoomStatus;
import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.GameEventType;
import com.gamehub.game.exception.GameRuleViolationException;
import com.gamehub.game.exception.UnauthorizedException;
import com.gamehub.game.fishpond.dto.FishpondDtos.ChatState;
import com.gamehub.game.fishpond.dto.FishpondDtos.GameState;
import com.gamehub.game.fishpond.dto.FishpondDtos.MessageState;
import com.gamehub.game.fishpond.dto.FishpondDtos.PlayerState;
import com.gamehub.game.fishpond.dto.FishpondDtos.StartResponse;
import com.gamehub.game.exception.GameSessionNotFoundException;
import com.gamehub.repository.GameRoomRepository;
import com.gamehub.repository.RoomPlayerRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FishpondGameService {

    private static final String ROOM_TOPIC = "/topic/room/";
    private static final int MAX_MESSAGE_LENGTH = 280;

    private final GameRoomRepository gameRoomRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, FishpondSession> sessions = new ConcurrentHashMap<>();

    public FishpondGameService(
            GameRoomRepository gameRoomRepository,
            RoomPlayerRepository roomPlayerRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.gameRoomRepository = gameRoomRepository;
        this.roomPlayerRepository = roomPlayerRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public StartResponse start(String roomCode, User user) {
        GameRoom room = findRoom(roomCode);
        requireHost(room, user);

        if (!"fishpond".equals(room.getGame().getSlug())) {
            throw new GameRuleViolationException("This room is not a Fishpond game");
        }
        if (room.getRoomStatus() != RoomStatus.WAITING) {
            throw new GameRuleViolationException("This room has already started");
        }

        List<RoomPlayer> roomPlayers = roomPlayerRepository.findByRoom(room);
        if (roomPlayers.size() < 3) {
            throw new GameRuleViolationException("Fishpond needs at least 3 players");
        }

        FishpondSession session = new FishpondSession(room, roomPlayers);
        sessions.put(roomCode, session);
        room.setRoomStatus(RoomStatus.IN_PROGRESS);
        gameRoomRepository.save(room);

        publish(session, "FISHPOND_STARTED", session.getSessionId());
        return new StartResponse(session.getSessionId());
    }

    public GameState getState(String roomCode, User user) {
        FishpondSession session = requireSession(roomCode);
        synchronized (session) {
            Participant participant = requireParticipant(session, user);
            return state(session, participant);
        }
    }

    public boolean canRejoin(String roomCode, Long userId) {
        FishpondSession session = sessions.get(roomCode);
        if (session == null) {
            return false;
        }

        synchronized (session) {
            Participant participant = session.participants.get(userId);
            return session.phase == Phase.SUBMITTING
                    && participant != null
                    && participant.left;
        }
    }

    public void playerRejoined(String roomCode, User user) {
        FishpondSession session = requireSession(roomCode);
        synchronized (session) {
            Participant participant = session.participants.get(user.getUserId());
            if (session.phase != Phase.SUBMITTING || participant == null || !participant.left) {
                throw new GameRuleViolationException(
                        "You can only rejoin Fishpond before message reading starts"
                );
            }

            participant.left = false;
            publish(session, "PLAYER_REJOINED", user.getUserId());
            publish(session, "FISHPOND_UPDATED", null);
        }
    }

    public GameState submit(String roomCode, User user, String rawMessage) {
        FishpondSession session = requireSession(roomCode);
        synchronized (session) {
            Participant participant = requireParticipant(session, user);
            String message = rawMessage == null ? "" : rawMessage.trim();

            if (session.phase != Phase.SUBMITTING) {
                throw new GameRuleViolationException("Message submission is closed");
            }
            if (message.isEmpty()) {
                throw new GameRuleViolationException("Write a message before submitting");
            }
            if (message.length() > MAX_MESSAGE_LENGTH) {
                throw new GameRuleViolationException("Messages must be 280 characters or fewer");
            }
            if (session.messages.containsKey(user.getUserId())) {
                throw new GameRuleViolationException("You have already submitted your message");
            }

            session.messages.put(user.getUserId(), new FishpondMessage(
                    UUID.randomUUID(),
                    user.getUserId(),
                    participant.displayName,
                    message
            ));

            beginReadingWhenReady(session);
            publish(session, "FISHPOND_UPDATED", null);
            return state(session, participant);
        }
    }

    public GameState navigate(String roomCode, User user, String action) {
        FishpondSession session = requireSession(roomCode);
        synchronized (session) {
            Participant participant = requireParticipant(session, user);
            requireHost(session, user);

            if (session.phase != Phase.READING) {
                throw new GameRuleViolationException("Messages are not ready to read yet");
            }

            if ("PREVIOUS".equalsIgnoreCase(action) && session.currentIndex > 0) {
                session.currentIndex--;
            } else if ("NEXT".equalsIgnoreCase(action)
                    && session.currentIndex < session.order.size() - 1) {
                session.currentIndex++;
            } else if (!"PREVIOUS".equalsIgnoreCase(action)
                    && !"NEXT".equalsIgnoreCase(action)) {
                throw new GameRuleViolationException("Choose PREVIOUS or NEXT");
            }

            publish(session, "FISHPOND_UPDATED", null);
            return state(session, participant);
        }
    }

    public GameState revealAuthors(String roomCode, User user) {
        FishpondSession session = requireSession(roomCode);
        synchronized (session) {
            Participant participant = requireParticipant(session, user);
            requireHost(session, user);

            if ((session.phase != Phase.READING && session.phase != Phase.COMPLETE)
                    || session.currentIndex != session.order.size() - 1) {
                throw new GameRuleViolationException(
                "Author visibility can only be changed on the final message"
                );
            }

            session.authorsRevealed = !session.authorsRevealed;
            session.phase = session.authorsRevealed ? Phase.COMPLETE : Phase.READING;
            session.room.setRoomStatus(
                    RoomStatus.WAITING
            );
            gameRoomRepository.save(session.room);
            publish(
                session,
                session.authorsRevealed ? "AUTHORS_REVEALED" : "AUTHORS_HIDDEN",
                null
            );
            return state(session, participant);
        }
    }

    public GameState sendChat(String roomCode, User user, String rawMessage) {
        FishpondSession session = requireSession(roomCode);
        synchronized (session) {
            Participant participant = requireParticipant(session, user);
            String message = rawMessage == null ? "" : rawMessage.trim();

            if (session.phase == Phase.COMPLETE) {
                throw new GameRuleViolationException("Chat is closed for this game");
            }
            if (message.isEmpty()) {
                throw new GameRuleViolationException("Write a chat message first");
            }
            if (message.length() > MAX_MESSAGE_LENGTH) {
                throw new GameRuleViolationException("Chat messages must be 280 characters or fewer");
            }

            session.chatMessages.add(new FishpondChatMessage(
                    UUID.randomUUID(),
                    user.getUserId(),
                    participant.displayName,
                    message,
                    Instant.now()
            ));
                    publish(
                        session,
                        "CHAT_MESSAGE",
                        session.chatMessages.get(session.chatMessages.size() - 1).toResponse()
                    );
            return state(session, participant);
        }
    }

    public void playerLeft(GameRoom room, User user, User newHost) {
        FishpondSession session = sessions.get(room.getRoomCode());
        if (session == null) {
            return;
        }

        synchronized (session) {
            Participant participant = session.participants.get(user.getUserId());
            if (participant == null || participant.left) {
                return;
            }
            participant.left = true;
            if (user.getUserId().equals(session.hostUserId) && newHost != null) {
                session.hostUserId = newHost.getUserId();
                session.hostUsername = newHost.getUsername();
            }
            boolean noPlayersRemain = session.participants.values().stream()
                    .noneMatch(activeParticipant -> !activeParticipant.left);
            if (noPlayersRemain) {
                session.phase = Phase.CANCELLED;
                room.setRoomStatus(RoomStatus.CANCELLED);
                gameRoomRepository.save(room);
                publish(session, "FISHPOND_CANCELLED", null);
                return;
            }
            if (session.phase == Phase.SUBMITTING) {
                beginReadingWhenReady(session);
            }
            publish(session, "PLAYER_LEFT", user.getUserId());
            publish(session, "FISHPOND_UPDATED", null);
        }
    }

    private void beginReadingWhenReady(FishpondSession session) {
        boolean allSubmitted = session.participants.values().stream()
                .filter(participant -> !participant.left)
                .allMatch(participant -> session.messages.containsKey(participant.userId));
        if (!allSubmitted || session.messages.isEmpty()) {
            return;
        }

        session.order = new ArrayList<>(session.messages.keySet());
        Collections.shuffle(session.order);
        session.currentIndex = 0;
        session.phase = Phase.READING;
    }

    private GameState state(FishpondSession session, Participant currentPlayer) {
        boolean authorsRevealed = session.authorsRevealed;
        FishpondMessage current = session.currentMessage();
        List<MessageState> revealedMessages = authorsRevealed
                ? session.order.stream()
                        .map(session.messages::get)
                        .map(message -> message.toResponse(true))
                        .toList()
                : List.of();

        List<PlayerState> players = session.participants.values().stream()
                .map(participant -> new PlayerState(
                        participant.userId,
                        participant.displayName,
                        session.messages.containsKey(participant.userId),
                        participant.left
                ))
                .toList();

        List<ChatState> chats = session.chatMessages.stream()
                .map(FishpondChatMessage::toResponse)
                .toList();

        boolean isFinalMessage = session.currentIndex == session.order.size() - 1;

        return new GameState(
                session.getSessionId(),
                session.phase.name(),
                session.hostUsername,
                session.hostUserId.equals(currentPlayer.userId),
                session.messages.containsKey(currentPlayer.userId),
                (int) session.participants.values().stream().filter(p -> !p.left).count(),
                session.messages.size(),
                session.currentIndex,
                current == null ? null : current.toResponse(authorsRevealed),
                session.phase == Phase.READING && session.currentIndex > 0,
                session.phase == Phase.READING && session.currentIndex < session.order.size() - 1,
                session.hostUserId.equals(currentPlayer.userId)
                    && isFinalMessage
                    && (session.phase == Phase.READING || session.phase == Phase.COMPLETE),
                authorsRevealed,
                players,
                revealedMessages,
                chats
        );
    }

    private GameRoom findRoom(String roomCode) {
        return gameRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new GameRuleViolationException("Room not found"));
    }

    private FishpondSession requireSession(String roomCode) {
        FishpondSession session = sessions.get(roomCode);
        if (session == null) {
            throw new GameSessionNotFoundException("Fishpond session is no longer available");
        }
        return session;
    }

    private Participant requireParticipant(FishpondSession session, User user) {
        Participant participant = session.participants.get(user.getUserId());
        if (participant == null || participant.left) {
            throw new UnauthorizedException("You are not an active player in this Fishpond game");
        }
        return participant;
    }

    private void requireHost(FishpondSession session, User user) {
        if (!session.hostUserId.equals(user.getUserId())) {
            throw new UnauthorizedException("Only the host can control the Fishpond game");
        }
    }

    private void requireHost(GameRoom room, User user) {
        if (!room.getHost().getUserId().equals(user.getUserId())) {
            throw new UnauthorizedException("Only the host can start the Fishpond game");
        }
    }

    private void publish(FishpondSession session, String type, Object data) {
        messagingTemplate.convertAndSend(
                ROOM_TOPIC + session.roomCode,
                new FishpondEvent(type, session.getSessionId(), session.roomCode, data)
        );
    }

    private enum Phase {
        SUBMITTING,
        READING,
        COMPLETE,
        CANCELLED
    }

    private static final class Participant {
        private final Long userId;
        private final String displayName;
        private boolean left;

        private Participant(User user) {
            this.userId = user.getUserId();
            this.displayName = user.getDisplayName() == null
                    ? user.getUsername()
                    : user.getDisplayName();
        }
    }

    private static final class FishpondMessage {
        private final UUID id;
        private final Long authorId;
        private final String authorName;
        private final String message;

        private FishpondMessage(UUID id, Long authorId, String authorName, String message) {
            this.id = id;
            this.authorId = authorId;
            this.authorName = authorName;
            this.message = message;
        }

        private MessageState toResponse(boolean revealAuthor) {
            return new MessageState(id, message, revealAuthor ? authorName : null);
        }
    }

    private static final class FishpondChatMessage {
        private final UUID id;
        private final Long userId;
        private final String displayName;
        private final String message;
        private final Instant sentAt;

        private FishpondChatMessage(
                UUID id,
                Long userId,
                String displayName,
                String message,
                Instant sentAt
        ) {
            this.id = id;
            this.userId = userId;
            this.displayName = displayName;
            this.message = message;
            this.sentAt = sentAt;
        }

        private ChatState toResponse() {
            return new ChatState(id, userId, displayName, message, sentAt);
        }
    }

    private static final class FishpondSession {
        private final String roomCode;
        private final GameRoom room;
        private final UUID sessionId = UUID.randomUUID();
        private final Map<Long, Participant> participants = new LinkedHashMap<>();
        private final Map<Long, FishpondMessage> messages = new LinkedHashMap<>();
        private final List<FishpondChatMessage> chatMessages = new ArrayList<>();
        private List<Long> order = new ArrayList<>();
        private Long hostUserId;
        private String hostUsername;
        private Phase phase = Phase.SUBMITTING;
        private int currentIndex = -1;
        private boolean authorsRevealed;

        private FishpondSession(GameRoom room, List<RoomPlayer> roomPlayers) {
            this.room = room;
            this.roomCode = room.getRoomCode();
            this.hostUserId = room.getHost().getUserId();
            this.hostUsername = room.getHost().getUsername();
            for (RoomPlayer roomPlayer : roomPlayers) {
                Participant participant = new Participant(roomPlayer.getUser());
                participants.put(participant.userId, participant);
            }
        }

        private UUID getSessionId() {
            return sessionId;
        }

        private FishpondMessage currentMessage() {
            if (currentIndex < 0 || currentIndex >= order.size()) {
                return null;
            }
            return messages.get(order.get(currentIndex));
        }
    }

    private record FishpondEvent(String type, UUID sessionId, String roomCode, Object data) {
    }
}