package com.gamehub.game.deception.service;

import com.gamehub.game.deception.domain.GameEventType;
import com.gamehub.game.deception.domain.GameSession;
import com.gamehub.game.deception.dto.GameEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameEventService {

    private final SimpMessagingTemplate messagingTemplate;

    public GameEventService(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Sends an event to everyone watching this game session.
     */
    public void publish(
            GameSession gameSession,
            GameEventType type,
            Object data
    ) {

        GameEvent event =
                new GameEvent(
                        type,
                        gameSession.getSessionId(),
                        data
                );

        messagingTemplate.convertAndSend(
                "/topic/game/" +
                        gameSession.getSessionId(),
                event
        );
    }
}