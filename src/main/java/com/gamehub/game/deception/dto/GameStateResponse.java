package com.gamehub.game.deception.dto;

import com.gamehub.game.deception.domain.DeceptionRole;
import com.gamehub.game.deception.domain.GamePhase;

import java.util.List;
import java.util.UUID;

public class GameStateResponse {

    private UUID sessionId;
    private GamePhase gamePhase;
    private int roundNumber;

    // Only the requesting player's role is exposed.
    private DeceptionRole yourRole;

    // Public information about all players.
    private List<GamePlayerResponse> players;

    public GameStateResponse(
            UUID sessionId,
            GamePhase gamePhase,
            int roundNumber,
            DeceptionRole yourRole,
            List<GamePlayerResponse> players
    ) {
        this.sessionId = sessionId;
        this.gamePhase = gamePhase;
        this.roundNumber = roundNumber;
        this.yourRole = yourRole;
        this.players = players;
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

    public DeceptionRole getYourRole() {
        return yourRole;
    }

    public List<GamePlayerResponse> getPlayers() {
        return players;
    }
}