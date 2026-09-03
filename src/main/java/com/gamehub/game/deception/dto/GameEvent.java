package com.gamehub.game.deception.dto;

import com.gamehub.game.deception.domain.GameEventType;

import java.util.UUID;

public class GameEvent {

    private GameEventType type;
    private UUID sessionId;
    private Object data;

    public GameEvent(
            GameEventType type,
            UUID sessionId,
            Object data
    ) {
        this.type = type;
        this.sessionId = sessionId;
        this.data = data;
    }

    public GameEventType getType() {
        return type;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public Object getData() {
        return data;
    }
}