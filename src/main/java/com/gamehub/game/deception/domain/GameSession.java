package com.gamehub.game.deception.domain;


import com.gamehub.domain.room.GameRoom;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameSession {
    private UUID sessionId = UUID.randomUUID();
    private GameRoom gameRoom;
    private Map<Long,GamePlayer> players = new HashMap<>();
    private GamePhase gamePhase = GamePhase.NIGHT;
    private int roundNumber = 1;
    private DeceptionGameConfig deceptionGameConfig;

    public GameSession(GameRoom gameRoom , DeceptionGameConfig deceptionGameConfig){
        this.gameRoom=gameRoom;
        this.deceptionGameConfig=deceptionGameConfig;
    }

    public void addPlayer(GamePlayer player){
        players.put(player.getUser().getUserId() , player);
    }
}
