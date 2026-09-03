package com.gamehub.game.deception.service;

import com.gamehub.game.deception.domain.DeceptionRole;
import com.gamehub.game.deception.domain.GameEventType;
import com.gamehub.game.deception.domain.GamePhase;
import com.gamehub.game.deception.domain.GamePlayer;
import com.gamehub.game.deception.domain.GameSession;
import com.gamehub.game.deception.domain.NightAction;
import com.gamehub.game.deception.domain.NightActionEntry;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class NightResolutionService {

    private final WinConditionService winConditionService;
    private final GameEventService gameEventService;

    public NightResolutionService(
            WinConditionService winConditionService,
            GameEventService gameEventService
    ) {
        this.winConditionService = winConditionService;
        this.gameEventService = gameEventService;
    }

    /**
     * Checks whether every living special-role player
     * has submitted their night action.
     *
     * Required actions:
     * - Every living Predator
     * - Living Doctor, if present
     * - Living Detective, if present
     */
    public boolean isNightReady(GameSession gameSession) {

        int requiredActions = 0;

        for (GamePlayer player :
                gameSession.getPlayers().values()) {

            // Dead players do not need to submit actions.
            if (!player.isAlive()) {
                continue;
            }

            if (player.getRole() == DeceptionRole.PREDATOR) {
                requiredActions++;
            }

            if (player.getRole() == DeceptionRole.DOCTOR) {
                requiredActions++;
            }

            if (player.getRole() == DeceptionRole.DETECTIVE) {
                requiredActions++;
            }
        }

        return gameSession.getNightActions().size()
                >= requiredActions;
    }


    /**
     * Resolves all submitted night actions.
     *
     * Order:
     * 1. Determine Predator target
     * 2. Determine Doctor protection
     * 3. Determine Detective investigation
     * 4. Apply elimination
     * 5. Store investigation result
     * 6. Check win condition
     * 7. Move to DAY if game is still active
     */
    public void resolveNight(GameSession gameSession) {

        Long predatorTarget =
                determinePredatorTarget(gameSession);

        Long doctorTarget =
                determineDoctorTarget(gameSession);

        Long detectiveTarget =
                determineDetectiveTarget(gameSession);


        // -------------------------------------------------
        // Store Doctor protection
        // -------------------------------------------------

        gameSession.setProtectedUserId(
                doctorTarget
        );


        // -------------------------------------------------
        // Resolve Predator elimination
        // -------------------------------------------------

        if (predatorTarget != null &&
                !predatorTarget.equals(doctorTarget)) {

            GamePlayer target =
                    gameSession.getPlayers()
                            .get(predatorTarget);

            if (target != null &&
                    target.isAlive()) {

                // Player was not protected.
                target.setAlive(false);

                gameSession.setEliminatedUserId(
                        predatorTarget
                );

                // Notify all connected players.
                gameEventService.publish(
                        gameSession,
                        GameEventType.PLAYER_ELIMINATED,
                        predatorTarget
                );
            }
        }


        // -------------------------------------------------
        // Resolve Detective investigation
        // -------------------------------------------------

        if (detectiveTarget != null) {

            GamePlayer investigatedPlayer =
                    gameSession.getPlayers()
                            .get(detectiveTarget);

            if (investigatedPlayer != null) {

                gameSession.setInvestigatedUserId(
                        detectiveTarget
                );

                gameSession.setInvestigationResult(
                        investigatedPlayer.getRole()
                );

                /*
                 * IMPORTANT:
                 *
                 * We DO NOT broadcast the investigation result.
                 *
                 * The result is private and can only be requested
                 * through the Detective-only REST endpoint.
                 */
            }
        }


        // -------------------------------------------------
        // NIGHT → DAY
        // -------------------------------------------------

        boolean gameOver =
                winConditionService.checkWinCondition(gameSession);

        if (gameOver) {

            // Game ended because of the night elimination.
            gameEventService.publish(
                    gameSession,
                    GameEventType.GAME_OVER,
                    gameSession.getGameResult()
            );

            return;
        }


        // No winner yet → continue to DAY.
        gameSession.setGamePhase(GamePhase.DAY);

        // Tell all clients that night resolution is complete.
        gameEventService.publish(
                gameSession,
                GameEventType.NIGHT_RESOLVED,
                gameSession.getEliminatedUserId()
        );

        // Tell all clients that DAY has started.
        gameEventService.publish(
                gameSession,
                GameEventType.DAY_STARTED,
                gameSession.getEliminatedUserId()
        );
    }


    /**
     * Determines the Predator team's target.
     *
     * Multiple Predators vote.
     *
     * Majority target wins.
     * If there is a tie, nobody is eliminated.
     */
    private Long determinePredatorTarget(
            GameSession gameSession
    ) {

        Map<Long, Integer> targetVotes =
                new HashMap<>();

        for (Map.Entry<Long, NightActionEntry> entry :
                gameSession.getNightActions().entrySet()) {

            Long playerId = entry.getKey();

            NightActionEntry action =
                    entry.getValue();

            GamePlayer predator =
                    gameSession.getPlayers()
                            .get(playerId);

            // Ignore missing/dead players.
            if (predator == null ||
                    !predator.isAlive()) {
                continue;
            }

            // Only Predators participate in elimination.
            if (predator.getRole()
                    != DeceptionRole.PREDATOR) {
                continue;
            }

            // Only ELIMINATE actions matter here.
            if (action.getAction()
                    != NightAction.ELIMINATE) {
                continue;
            }

            Long target =
                    action.getTargetUserId();

            targetVotes.put(
                    target,
                    targetVotes.getOrDefault(
                            target,
                            0
                    ) + 1
            );
        }

        // No Predator target.
        if (targetVotes.isEmpty()) {
            return null;
        }

        int highestVotes = 0;
        Long selectedTarget = null;
        boolean tie = false;

        for (Map.Entry<Long, Integer> entry :
                targetVotes.entrySet()) {

            int votes = entry.getValue();

            if (votes > highestVotes) {

                highestVotes = votes;
                selectedTarget = entry.getKey();
                tie = false;

            } else if (votes == highestVotes) {

                tie = true;
            }
        }

        // Tie between Predator targets → nobody dies.
        if (tie) {
            return null;
        }

        return selectedTarget;
    }


    /**
     * Finds the Doctor's protection target.
     */
    private Long determineDoctorTarget(
            GameSession gameSession
    ) {

        for (Map.Entry<Long, NightActionEntry> entry :
                gameSession.getNightActions().entrySet()) {

            GamePlayer player =
                    gameSession.getPlayers()
                            .get(entry.getKey());

            if (player == null ||
                    !player.isAlive()) {
                continue;
            }

            if (player.getRole()
                    == DeceptionRole.DOCTOR &&
                    entry.getValue().getAction()
                            == NightAction.PROTECT) {

                return entry.getValue()
                        .getTargetUserId();
            }
        }

        return null;
    }


    /**
     * Finds the Detective's investigation target.
     */
    private Long determineDetectiveTarget(
            GameSession gameSession
    ) {

        for (Map.Entry<Long, NightActionEntry> entry :
                gameSession.getNightActions().entrySet()) {

            GamePlayer player =
                    gameSession.getPlayers()
                            .get(entry.getKey());

            if (player == null ||
                    !player.isAlive()) {
                continue;
            }

            if (player.getRole()
                    == DeceptionRole.DETECTIVE &&
                    entry.getValue().getAction()
                            == NightAction.INVESTIGATE) {

                return entry.getValue()
                        .getTargetUserId();
            }
        }

        return null;
    }
}