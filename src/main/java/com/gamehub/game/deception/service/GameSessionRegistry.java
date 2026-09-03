package com.gamehub.game.deception.service;

import com.gamehub.game.deception.domain.GameSession;
import org.springframework.stereotype.Service;


import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionRegistry {
    private final Map<UUID,GameSession> gameSessionMap = new ConcurrentHashMap<>();
    // why concurrent hashmap?
    // Map designed for safe concurrent access by multiple threads.

    public void storeSession(GameSession gameSession){
        gameSessionMap.put(gameSession.getSessionId(),gameSession);
    }

    public GameSession retrieveSession(UUID sessionId){
        return gameSessionMap.get(sessionId);
    }
}
