
        package com.gamehub.game.deception.service;

import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.*;
import com.gamehub.game.deception.dto.NightActionRequest;
import com.gamehub.game.deception.dto.NightActivityResponse;
import org.springframework.stereotype.Service;

@Service
public class NightActionService {

    private final NightResolutionService nightResolutionService;
    private final GameEventService gameEventService;

    public NightActionService(
            NightResolutionService nightResolutionService,
            GameEventService gameEventService
    ) {
        this.nightResolutionService = nightResolutionService;
        this.gameEventService = gameEventService;
    }

    public String performAction(
            GameSession gameSession,
            User user,
            NightAction action,
            NightActionRequest request
    ) {

        // -------------------------------------------------
        // 1. Find current player
        // -------------------------------------------------

        GamePlayer currentPlayer =
                gameSession.getPlayers()
                        .get(user.getUserId());

        if (currentPlayer == null) {
            throw new RuntimeException(
                    "Player not found in game"
            );
        }

        // -------------------------------------------------
        // 2. Dead players cannot perform actions
        // -------------------------------------------------

        if (!currentPlayer.isAlive()) {
            throw new RuntimeException(
                    "Dead players cannot perform actions"
            );
        }

        // -------------------------------------------------
        // 3. Actions only allowed during NIGHT
        // -------------------------------------------------

        if (gameSession.getGamePhase()
                != GamePhase.NIGHT) {

            throw new RuntimeException(
                    "Night actions are only allowed during NIGHT phase"
            );
        }

        // -------------------------------------------------
        // 4. Validate request
        // -------------------------------------------------

        if (
                request == null ||
                        request.getTargetUserId() == null
        ) {
            throw new RuntimeException(
                    "Target player is required"
            );
        }

        Long targetUserId =
                request.getTargetUserId();

        GamePlayer targetPlayer =
                gameSession.getPlayers()
                        .get(targetUserId);

        if (targetPlayer == null) {
            throw new RuntimeException(
                    "Target player not found"
            );
        }

        // -------------------------------------------------
        // 5. Target must be alive
        // -------------------------------------------------

        if (!targetPlayer.isAlive()) {
            throw new RuntimeException(
                    "Cannot target a dead player"
            );
        }

        // -------------------------------------------------
        // 6. Self-target validation
        //
        // Doctor may protect themselves.
        // Predator and Detective may NOT target themselves.
        // -------------------------------------------------

        if (
                currentPlayer.getUserId().equals(targetUserId)
                        && action != NightAction.PROTECT
        ) {
            throw new RuntimeException(
                    "Player cannot target themselves"
            );
        }

        // -------------------------------------------------
        // 7. Validate role vs action
        // -------------------------------------------------

        validateRoleAction(
                currentPlayer,
                action
        );

        // -------------------------------------------------
        // 8. Store action
        // -------------------------------------------------

        gameSession.recordNightAction(
                user.getUserId(),
                action,
                targetUserId
        );

        gameEventService.publish(
                gameSession,
                GameEventType.NIGHT_ACTIVITY,
                new NightActivityResponse(
                        "A player has completed their night action."
                )
        );

        // -------------------------------------------------
        // 9. Check whether everyone required has acted
        // -------------------------------------------------

        if (
                nightResolutionService
                        .isNightReady(gameSession)
        ) {

            nightResolutionService
                    .resolveNight(gameSession);

            return "Night resolved. Day phase started.";
        }

        return "Night action submitted. Waiting for other players.";
    }


    private void validateRoleAction(
            GamePlayer player,
            NightAction action
    ) {

        DeceptionRole role =
                player.getRole();

        switch (action) {

            case ELIMINATE -> {

                if (
                        role !=
                                DeceptionRole.PREDATOR
                ) {
                    throw new RuntimeException(
                            "Only Predators can perform ELIMINATE action"
                    );
                }
            }

            case PROTECT -> {

                if (
                        role !=
                                DeceptionRole.DOCTOR
                ) {
                    throw new RuntimeException(
                            "Only Doctor can perform PROTECT action"
                    );
                }
            }

            case INVESTIGATE -> {

                if (
                        role !=
                                DeceptionRole.DETECTIVE
                ) {
                    throw new RuntimeException(
                            "Only Detective can perform INVESTIGATE action"
                    );
                }
            }
        }
    }
}

