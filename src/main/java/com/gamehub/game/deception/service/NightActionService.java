package com.gamehub.game.deception.service;

import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.GamePlayer;
import com.gamehub.game.deception.domain.GameSession;
import com.gamehub.game.deception.domain.NightAction;
import com.gamehub.game.deception.dto.NightActionRequest;
import org.springframework.stereotype.Service;

@Service
public class NightActionService {
    public void performAction(
            GameSession gameSession,
            User user,
            NightAction action,
            NightActionRequest request
    ){
        // Find the player performing the action.
        GamePlayer currentPlayer =
                gameSession.getPlayers().get(user.getUserId());

        // Player must belong to the active game.
        if (currentPlayer == null) {
            throw new RuntimeException("Player not found in game");
        }

        // Only alive players can perform night actions.
        if (!currentPlayer.isAlive()) {
            throw new RuntimeException("Dead players cannot perform actions");
        }

        // Night actions can only happen during NIGHT phase.
        if (gameSession.getGamePhase() !=
                com.gamehub.game.deception.domain.GamePhase.NIGHT) {

            throw new RuntimeException(
                    "Night actions are only allowed during NIGHT phase"
            );
        }

        // Find the target.
        GamePlayer targetPlayer =
                gameSession.getPlayers().get(request.getTargetUserId());

        if (targetPlayer == null) {
            throw new RuntimeException("Target player not found");
        }

        // A player cannot target themselves.
        if (targetPlayer.getUser().getUserId()
                .equals(user.getUserId())) {

            throw new RuntimeException(
                    "Player cannot target themselves"
            );
        }

        // Target must be alive.
        if (!targetPlayer.isAlive()) {
            throw new RuntimeException(
                    "Cannot target a dead player"
            );
        }

        // Role determines which action the player is allowed to perform.
        switch (action) {

            case ELIMINATE -> {
                if (currentPlayer.getRole() !=
                        com.gamehub.game.deception.domain.DeceptionRole.PREDATOR) {

                    throw new RuntimeException(
                            "Only Predators can perform ELIMINATE action"
                    );
                }

                // Actual kill resolution comes later.
                gameSession.recordNightAction(
                        user.getUserId(),
                        action,
                        request.getTargetUserId()
                );
            }

            case PROTECT -> {
                if (currentPlayer.getRole() !=
                        com.gamehub.game.deception.domain.DeceptionRole.DOCTOR) {

                    throw new RuntimeException(
                            "Only Doctor can perform PROTECT action"
                    );
                }

                // Actual protection resolution comes later.
                gameSession.recordNightAction(
                        user.getUserId(),
                        action,
                        request.getTargetUserId()
                );
            }

            case INVESTIGATE -> {
                if (currentPlayer.getRole() !=
                        com.gamehub.game.deception.domain.DeceptionRole.DETECTIVE) {

                    throw new RuntimeException(
                            "Only Detective can perform INVESTIGATE action"
                    );
                }

                // Actual investigation result comes later.
                gameSession.recordNightAction(
                        user.getUserId(),
                        action,
                        request.getTargetUserId()
                );
            }
        }
    }
}
