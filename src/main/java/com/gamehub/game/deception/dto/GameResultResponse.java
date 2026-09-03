package com.gamehub.game.deception.dto;

import com.gamehub.game.deception.domain.GameResult;

import java.util.UUID;

public class GameResultResponse {

    private UUID sessionId;
    private GameResult result;

    public GameResultResponse(
            UUID sessionId,
            GameResult result
    ) {
        this.sessionId = sessionId;
        this.result = result;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public GameResult getResult() {
        return result;
    }
}