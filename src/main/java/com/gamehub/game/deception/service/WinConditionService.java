package com.gamehub.game.deception.service;

import com.gamehub.domain.room.RoomStatus;
import com.gamehub.game.deception.domain.*;
import com.gamehub.repository.GameRoomRepository;
import org.springframework.stereotype.Service;

@Service
public class WinConditionService {

    private final GameRoomRepository gameRoomRepository;

    public WinConditionService(
            GameRoomRepository gameRoomRepository
    ) {
        this.gameRoomRepository = gameRoomRepository;
    }

    private void finishGame(
            GameSession gameSession,
            GameResult result
    ) {

        gameSession.setGameResult(result);
        gameSession.setGamePhase(GamePhase.GAME_OVER);

        gameSession.getGameRoom()
                .setRoomStatus(RoomStatus.FINISHED);

        gameRoomRepository.save(
                gameSession.getGameRoom()
        );
    }

    public boolean checkWinCondition(
            GameSession gameSession
    ) {

        int alivePredators = 0;
        int aliveOthers = 0;

        for (GamePlayer player :
                gameSession.getPlayers().values()) {

            if (!player.isAlive()) {
                continue;
            }

            if (player.getRole()
                    == DeceptionRole.PREDATOR) {

                alivePredators++;

            } else {

                aliveOthers++;
            }
        }

        // All non-Predators eliminated.
        if (aliveOthers == 0) {

            gameSession.setGameResult(
                    GameResult.PREDATORS_WIN
            );

            gameSession.setGamePhase(
                    GamePhase.GAME_OVER
            );

            return true;
        }

        // Predators eliminated.
        if (alivePredators == 0) {

            finishGame(
                    gameSession,
                    GameResult.INNOCENTS_WIN
            );

            return true;
        }

        // Predators equal or outnumber the others.
        if (alivePredators >= aliveOthers) {

            finishGame(
                    gameSession,
                    GameResult.PREDATORS_WIN
            );

            return true;
        }

        return false;
    }
}