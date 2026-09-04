
package com.gamehub.game.deception.dto;

import com.gamehub.game.deception.domain.DeceptionRole;
import com.gamehub.game.deception.domain.GamePhase;

import java.util.List;
import java.util.UUID;

public class GameStateResponse {

    private UUID sessionId;

    private GamePhase gamePhase;

    private int roundNumber;

    private DeceptionRole yourRole;

    private String yourUsername;

    private boolean isHost;

    private Long hostUserId;

    private String hostUsername;

    private List<GamePlayerResponse> players;

    public GameStateResponse(
            UUID sessionId,
            GamePhase gamePhase,
            int roundNumber,
            DeceptionRole yourRole,
            List<GamePlayerResponse> players,
            String yourUsername,
            boolean isHost,
            Long hostUserId,
            String hostUsername
    ) {
        this.sessionId = sessionId;
        this.gamePhase = gamePhase;
        this.roundNumber = roundNumber;
        this.yourRole = yourRole;
        this.players = players;
        this.yourUsername = yourUsername;
        this.isHost = isHost;
        this.hostUserId = hostUserId;
        this.hostUsername = hostUsername;
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

    public String getYourUsername() {
        return yourUsername;
    }

    public boolean isHost() {
        return isHost;
    }

    public Long getHostUserId() {
        return hostUserId;
    }

    public String getHostUsername() {
        return hostUsername;
    }

    public List<GamePlayerResponse> getPlayers() {
        return players;
    }
}

