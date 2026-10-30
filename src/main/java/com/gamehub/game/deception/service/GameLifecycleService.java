package com.gamehub.game.deception.service;

import com.gamehub.domain.room.GameRoom;
import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.GameEventType;
import com.gamehub.game.deception.domain.GamePhase;
import com.gamehub.game.deception.domain.GameSession;
import org.springframework.stereotype.Service;

@Service
public class GameLifecycleService {

    private final GameSessionRegistry gameSessionRegistry;
    private final WinConditionService winConditionService;
    private final NightResolutionService nightResolutionService;
    private final VotingService votingService;
    private final GameEventService gameEventService;

    public GameLifecycleService(
            GameSessionRegistry gameSessionRegistry,
            WinConditionService winConditionService,
            NightResolutionService nightResolutionService,
            VotingService votingService,
            GameEventService gameEventService
    ) {
        this.gameSessionRegistry = gameSessionRegistry;
        this.winConditionService = winConditionService;
        this.nightResolutionService = nightResolutionService;
        this.votingService = votingService;
        this.gameEventService = gameEventService;
    }

    public void playerLeft(GameRoom room, User player, User newHost) {
        GameSession session =
                gameSessionRegistry.retrieveSessionByRoomCode(room.getRoomCode());

        if (session == null) {
            return;
        }

        if (session.markPlayerLeft(player.getUserId()) == null) {
            return;
        }

        if (player.getUserId().equals(session.getHostUserId())) {
            session.changeHost(newHost);
        }

        gameEventService.publish(
                session,
                GameEventType.PLAYER_LEFT,
                player.getUserId()
        );

        if (session.getGamePhase() == GamePhase.GAME_OVER) {
            return;
        }

        if (winConditionService.checkWinCondition(session)) {
            gameEventService.publish(
                    session,
                    GameEventType.GAME_OVER,
                    session.getGameResult()
            );
            return;
        }

        if (session.getGamePhase() == GamePhase.NIGHT
                && nightResolutionService.isNightReady(session)) {
            nightResolutionService.resolveNight(session);
            return;
        }

        votingService.resolveAfterPlayerLeft(session);
    }
}