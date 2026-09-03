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
}
