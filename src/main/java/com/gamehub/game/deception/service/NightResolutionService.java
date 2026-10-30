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
     * 1. Clear previous investigation data
     * 2. Determine Predator target
     * 3. Determine Doctor protection
     * 4. Determine Detective investigation
     * 5. Apply elimination
     * 6. Store investigation result
     * 7. Check win condition
     * 8. Move to DAY if game is still active
     */
    public void resolveNight(GameSession gameSession) {

        // -------------------------------------------------
        // Clear previous night-specific investigation data
        // -------------------------------------------------

        gameSession.setInvestigatedUserId(null);
        gameSession.setInvestigationResult(null);
        gameSession.setProtectedUserId(null);
        gameSession.setEliminatedUserId(null);


        // -------------------------------------------------
        // Determine night actions
        // -------------------------------------------------

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

                target.setAlive(false);

                gameSession.setEliminatedUserId(
                        predatorTarget
                );

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

            GamePlayer detective =
                    findLivingDetective(gameSession);

            /*
             * Only a living Detective may have
             * an investigation result.
             */
            if (detective != null) {

                GamePlayer investigatedPlayer =
                        gameSession.getPlayers()
                                .get(detectiveTarget);

                if (investigatedPlayer != null &&
                        investigatedPlayer.isAlive()) {

                    gameSession.setInvestigatedUserId(
                            detectiveTarget
                    );

                    gameSession.setInvestigationResult(
                            investigatedPlayer.getRole()
                    );
                }
            }
        }


        // -------------------------------------------------
        // NIGHT → DAY / GAME OVER
        // -------------------------------------------------

        boolean gameOver =
                winConditionService.checkWinCondition(
                        gameSession
                );

        if (gameOver) {

            gameEventService.publish(
                    gameSession,
                    GameEventType.GAME_OVER,
                    gameSession.getGameResult()
            );

            return;
        }


        // -------------------------------------------------
        // Start DAY
        // -------------------------------------------------

        gameSession.setGamePhase(
                GamePhase.DAY
        );


        gameEventService.publish(
                gameSession,
                GameEventType.NIGHT_RESOLVED,
                gameSession.getEliminatedUserId()
        );


        gameEventService.publish(
                gameSession,
                GameEventType.DAY_STARTED,
                gameSession.getEliminatedUserId()
        );
    }


    /**
     * Returns the currently living Detective.
     */
    private GamePlayer findLivingDetective(
            GameSession gameSession
    ) {

        for (GamePlayer player :
                gameSession.getPlayers().values()) {

            if (player.isAlive() &&
                    player.getRole()
                            == DeceptionRole.DETECTIVE) {

                return player;
            }
        }

        return null;
    }


    /**
     * Determines the Predator team's target.
     *
     * Multiple Predators vote.
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

            if (predator == null ||
                    !predator.isAlive()) {
                continue;
            }

            if (predator.getRole()
                    != DeceptionRole.PREDATOR) {
                continue;
            }

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