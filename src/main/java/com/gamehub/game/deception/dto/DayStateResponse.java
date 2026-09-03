package com.gamehub.game.deception.dto;

import com.gamehub.game.deception.domain.GamePhase;

import java.util.UUID;

public class DayStateResponse {

    private UUID sessionId;
    private GamePhase gamePhase;
    private int roundNumber;

    // Player eliminated during the previous night.
    private Long eliminatedUserId;

    public DayStateResponse(
            UUID sessionId,
            GamePhase gamePhase,
            int roundNumber,
            Long eliminatedUserId
    ) {
        this.sessionId = sessionId;
        this.gamePhase = gamePhase;
        this.roundNumber = roundNumber;
        this.eliminatedUserId = eliminatedUserId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public Long getEliminatedUserId() {
        return eliminatedUserId;
    }
}