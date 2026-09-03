package com.gamehub.game.deception.domain;


import com.gamehub.domain.room.GameRoom;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameSession {
    private UUID sessionId = UUID.randomUUID();
    private GameRoom gameRoom;
    private Map<Long,GamePlayer> players = new HashMap<>();
    private Map<Long, NightActionEntry> nightActions = new HashMap<>();
    private Map<Long, VoteEntry> votes = new HashMap<>();

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public Map<Long, GamePlayer> getPlayers() {
        return players;
    }

    public Map<Long, NightActionEntry> getNightActions() {
        return nightActions;
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }

    private GamePhase gamePhase = GamePhase.NIGHT;
    private int roundNumber = 1;
    private DeceptionGameConfig deceptionGameConfig;

    private Long protectedUserId;

    private Long eliminatedUserId;

    private Long investigatedUserId;

    private DeceptionRole investigationResult;

    private GameResult gameResult;


    public GameResult getGameResult() {
        return gameResult;
    }

    public void setGameResult(GameResult gameResult) {
        this.gameResult = gameResult;
    }

    public GameSession(GameRoom gameRoom , DeceptionGameConfig deceptionGameConfig){
        this.gameRoom=gameRoom;
        this.deceptionGameConfig=deceptionGameConfig;
    }


    public UUID getSessionId(){
        return sessionId;
    }

    public void addPlayer(GamePlayer player){
        players.put(player.getUser().getUserId() , player);
    }

    public void recordNightAction(
            Long userId,
            NightAction action,
            Long targetUserId
    ) {

        // A player can submit only ONE night action per night.
        if (nightActions.containsKey(userId)) {
            throw new RuntimeException(
                    "Player has already submitted a night action"
            );
        }

        nightActions.put(
                userId,
                new NightActionEntry(action, targetUserId)
        );
    }


    public void setGamePhase(GamePhase gamePhase) {
        this.gamePhase = gamePhase;
    }

    public Long getProtectedUserId() {
        return protectedUserId;
    }

    public void setProtectedUserId(Long protectedUserId) {
        this.protectedUserId = protectedUserId;
    }

    public Long getEliminatedUserId() {
        return eliminatedUserId;
    }

    public void setEliminatedUserId(Long eliminatedUserId) {
        this.eliminatedUserId = eliminatedUserId;
    }

    public Long getInvestigatedUserId() {
        return investigatedUserId;
    }

    public void setInvestigatedUserId(Long investigatedUserId) {
        this.investigatedUserId = investigatedUserId;
    }

    public DeceptionRole getInvestigationResult() {
        return investigationResult;
    }

    public void setInvestigationResult(
            DeceptionRole investigationResult
    ) {
        this.investigationResult = investigationResult;
    }


    public void startVoting() {

        if (gamePhase != GamePhase.DAY) {
            throw new RuntimeException(
                    "Voting can only start during DAY phase"
            );
        }

        gamePhase = GamePhase.VOTING;
    }

    public Map<Long, VoteEntry> getVotes() {
        return votes;
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

        // Move the game to the next round
        gamePhase = GamePhase.NIGHT;

        roundNumber++;

        // Night actions belong only to the current night.
        // Old actions must never affect the next round.
        nightActions.clear();

        // Reset temporary night information
        protectedUserId = null;
        eliminatedUserId = null;
        investigatedUserId = null;
        investigationResult = null;
    }
}
