
        package com.gamehub.game.deception.service;

import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.GameEventType;
import com.gamehub.game.deception.domain.GamePhase;
import com.gamehub.game.deception.domain.GamePlayer;
import com.gamehub.game.deception.domain.GameSession;
import com.gamehub.game.deception.dto.ChatMessageRequest;
import com.gamehub.game.deception.dto.ChatMessageResponse;

import com.gamehub.game.exception.GameSessionNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GameChatService {

    private final GameSessionRegistry gameSessionRegistry;
    private final GameEventService gameEventService;

    public GameChatService(
            GameSessionRegistry gameSessionRegistry,
            GameEventService gameEventService
    ) {
        this.gameSessionRegistry = gameSessionRegistry;
        this.gameEventService = gameEventService;
    }

    /**
     * Sends a message to everyone watching the game session.
     *
     * Chat is intentionally kept simple for Beta:
     *
     * - Available only during DAY.
     * - Only alive players can send messages.
     * - Dead players remain observers.
     * - Messages are not stored in the database.
     * - Messages are broadcast through the existing game event system.
     */
    public void sendMessage(
            UUID sessionId,
            User user,
            ChatMessageRequest request
    ) {

        // ---------------------------------------------------------
        // 1. Find game session
        // ---------------------------------------------------------

        GameSession gameSession =
                gameSessionRegistry.retrieveSession(sessionId);

        if (gameSession == null) {
            throw new GameSessionNotFoundException(
                    "Game Session Not Found"
            );
        }

        // ---------------------------------------------------------
        // 2. Validate request
        // ---------------------------------------------------------

        if (request == null ||
                request.getMessage() == null ||
                request.getMessage().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Message cannot be empty"
            );
        }

        String message = request.getMessage().trim();

        // Keep Beta chat messages reasonably small.
        if (message.length() > 300) {
            throw new IllegalArgumentException(
                    "Message cannot exceed 300 characters"
            );
        }

        // ---------------------------------------------------------
        // 3. Find player inside the game
        // ---------------------------------------------------------

        GamePlayer player =
                gameSession.getPlayers()
                        .get(user.getUserId());

        if (player == null) {

            throw new IllegalArgumentException(
                    "You are not a player in this game"
            );
        }

        // ---------------------------------------------------------
        // 4. Dead players are observers.
        //
        // They can still RECEIVE messages because they remain
        // subscribed to the game WebSocket topic.
        //
        // But they cannot SEND messages.
        // ---------------------------------------------------------

        if (!player.isAlive()) {

            throw new IllegalArgumentException(
                    "Dead players cannot send messages"
            );
        }

        // ---------------------------------------------------------
        // 5. Chat is available only during DAY.
        // ---------------------------------------------------------

        if (gameSession.getGamePhase() != GamePhase.DAY) {

            throw new IllegalArgumentException(
                    "Chat is only available during the day"
            );
        }

        // ---------------------------------------------------------
        // 6. Create public chat message
        // ---------------------------------------------------------

        ChatMessageResponse chatMessage =
                new ChatMessageResponse(
                        user.getUserId(),
                        user.getUsername(),
                        user.getDisplayName(),
                        user.getAvatarUrl(),
                        message
                );

        // ---------------------------------------------------------
        // 7. Broadcast to everyone in the game.
        //
        // This includes DEAD players as observers.
        // ---------------------------------------------------------

        gameEventService.publish(
                gameSession,
                GameEventType.CHAT_MESSAGE,
                chatMessage
        );
    }
}

