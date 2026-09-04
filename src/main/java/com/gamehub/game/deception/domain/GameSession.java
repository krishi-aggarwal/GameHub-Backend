
        package com.gamehub.game.deception.domain;

import com.gamehub.domain.room.GameRoom;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameSession {

    private UUID sessionId =
            UUID.randomUUID();

    private GameRoom gameRoom;

    private Map<Long, GamePlayer> players =
            new HashMap<>();

    private Map<Long, NightActionEntry> nightActions =
            new HashMap<>();

    private Map<Long, VoteEntry> votes =
            new HashMap<>();

    private GamePhase gamePhase =
            GamePhase.NIGHT;

    private int roundNumber = 1;

    private DeceptionGameConfig deceptionGameConfig;

    private Long protectedUserId;

    private Long eliminatedUserId;

    private Long investigatedUserId;

    private DeceptionRole investigationResult;

    private GameResult gameResult;

    /*
     * Store host information directly inside
     * the runtime game session.
     *
     * This prevents the game from depending on
     * a detached JPA GameRoom later.
     */
    private Long hostUserId;

    private String hostUsername;


    public GameSession(
            GameRoom gameRoom,
            DeceptionGameConfig deceptionGameConfig
    ) {

        this.gameRoom =
                gameRoom;

        this.deceptionGameConfig =
                deceptionGameConfig;

        if (gameRoom != null &&
                gameRoom.getHost() != null) {

            this.hostUserId =
                    gameRoom
                            .getHost()
                            .getUserId();

            this.hostUsername =
                    gameRoom
                            .getHost()
                            .getUsername();
        }
    }


    public UUID getSessionId() {
        return sessionId;
    }


    public GameRoom getGameRoom() {
        return gameRoom;
    }


    public Map<Long, GamePlayer> getPlayers() {
        return players;
    }


    public Map<Long, NightActionEntry> getNightActions() {
        return nightActions;
    }


    public Map<Long, VoteEntry> getVotes() {
        return votes;
    }


    public GamePhase getGamePhase() {
        return gamePhase;
    }


    public int getRoundNumber() {
        return roundNumber;
    }


    public DeceptionGameConfig getDeceptionGameConfig() {
        return deceptionGameConfig;
    }


    public Long getHostUserId() {
        return hostUserId;
    }


    public String getHostUsername() {
        return hostUsername;
    }


    public GameResult getGameResult() {
        return gameResult;
    }


    public void setGameResult(
            GameResult gameResult
    ) {
        this.gameResult =
                gameResult;
    }


    public void addPlayer(
            GamePlayer player
    ) {

        if (player == null) {
            throw new IllegalArgumentException(
                    "GamePlayer cannot be null"
            );
        }

        if (player.getUserId() == null) {
            throw new IllegalArgumentException(
                    "GamePlayer userId cannot be null"
            );
        }

        players.put(
                player.getUserId(),
                player
        );
    }


    public void recordNightAction(
            Long userId,
            NightAction action,
            Long targetUserId
    ) {

        if (nightActions.containsKey(userId)) {

            throw new RuntimeException(
                    "Player has already submitted a night action"
            );
        }

        nightActions.put(
                userId,
                new NightActionEntry(
                        action,
                        targetUserId
                )
        );
    }


    public void setGamePhase(
            GamePhase gamePhase
    ) {
        this.gamePhase =
                gamePhase;
    }


    public Long getProtectedUserId() {
        return protectedUserId;
    }


    public void setProtectedUserId(
            Long protectedUserId
    ) {
        this.protectedUserId =
                protectedUserId;
    }


    public Long getEliminatedUserId() {
        return eliminatedUserId;
    }


    public void setEliminatedUserId(
            Long eliminatedUserId
    ) {
        this.eliminatedUserId =
                eliminatedUserId;
    }


    public Long getInvestigatedUserId() {
        return investigatedUserId;
    }


    public void setInvestigatedUserId(
            Long investigatedUserId
    ) {
        this.investigatedUserId =
                investigatedUserId;
    }


    public DeceptionRole getInvestigationResult() {
        return investigationResult;
    }


    public void setInvestigationResult(
            DeceptionRole investigationResult
    ) {
        this.investigationResult =
                investigationResult;
    }


    public void startVoting() {

        if (gamePhase != GamePhase.DAY) {

            throw new RuntimeException(
                    "Voting can only start during DAY phase"
            );
        }

        gamePhase =
                GamePhase.VOTING;
    }


    public void recordVote(
            Long voterUserId,
            Long targetUserId
    ) {

        if (votes.containsKey(voterUserId)) {

            throw new RuntimeException(
                    "Player has already voted"
            );
        }

        votes.put(
                voterUserId,
                new VoteEntry(
                        voterUserId,
                        targetUserId
                )
        );
    }


    public void startNextNight() {

        gamePhase =
                GamePhase.NIGHT;

        roundNumber++;

        nightActions.clear();

        votes.clear();

        protectedUserId =
                null;

        eliminatedUserId =
                null;

        investigatedUserId =
                null;

        investigationResult =
                null;
    }
}

