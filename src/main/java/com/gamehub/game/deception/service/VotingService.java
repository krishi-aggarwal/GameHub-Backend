package com.gamehub.game.deception.service;

import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.*;
import com.gamehub.game.deception.dto.VoteRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class VotingService {

    private final WinConditionService winConditionService;
    private final GameEventService gameEventService;

    public VotingService(
            WinConditionService winConditionService,
            GameEventService gameEventService
    ) {
        this.winConditionService = winConditionService;
        this.gameEventService = gameEventService;
    }


    /**
     * Casts a vote for the current VOTING phase.
     *
     * Voting automatically resolves when
     * every living player has voted.
     */
    public String castVote(
            GameSession gameSession,
            User user,
            VoteRequest request
    ) {

        // -----------------------------------------
        // 1. Must be in VOTING phase
        // -----------------------------------------

        if (gameSession.getGamePhase()
                != GamePhase.VOTING) {

            throw new RuntimeException(
                    "Voting is not currently active"
            );
        }


        // -----------------------------------------
        // 2. Find voter
        // -----------------------------------------

        GamePlayer voter =
                gameSession.getPlayers()
                        .get(user.getUserId());

        if (voter == null) {

            throw new RuntimeException(
                    "Player not found in game"
            );
        }


        // -----------------------------------------
        // 3. Dead players cannot vote
        // -----------------------------------------

        if (!voter.isAlive()) {

            throw new RuntimeException(
                    "Dead players cannot vote"
            );
        }


        // -----------------------------------------
        // 4. Validate target
        // -----------------------------------------

        if (request == null ||
                request.getTargetUserId() == null) {

            throw new RuntimeException(
                    "Vote target is required"
            );
        }

        Long targetUserId =
                request.getTargetUserId();


        GamePlayer target =
                gameSession.getPlayers()
                        .get(targetUserId);


        if (target == null) {

            throw new RuntimeException(
                    "Target player not found"
            );
        }


        if (!target.isAlive()) {

            throw new RuntimeException(
                    "Cannot vote for a dead player"
            );
        }


        // -----------------------------------------
        // 5. Record vote
        // -----------------------------------------

        gameSession.recordVote(
                user.getUserId(),
                targetUserId
        );


        // -----------------------------------------
        // 6. Count living players
        // -----------------------------------------

        int alivePlayers = 0;

        for (GamePlayer player :
                gameSession.getPlayers().values()) {

            if (player.isAlive()) {
                alivePlayers++;
            }
        }


        // -----------------------------------------
        // 7. Resolve automatically when everyone
        //    alive has voted
        // -----------------------------------------

        if (gameSession.getVotes().size()
                >= alivePlayers) {

            resolveVoting(gameSession);

            return "Voting resolved";
        }


        return "Vote submitted";
    }


    /**
     * Resolves all votes.
     *
     * Highest vote count = eliminated.
     * Tie = nobody eliminated.
     */
    private void resolveVoting(
            GameSession gameSession
    ) {

        Map<Long, Integer> voteCounts =
                new HashMap<>();


        // -----------------------------------------
        // Count votes for each target
        // -----------------------------------------

        for (VoteEntry vote :
                gameSession.getVotes().values()) {

            Long target =
                    vote.getTargetUserId();

            voteCounts.put(
                    target,
                    voteCounts.getOrDefault(
                            target,
                            0
                    ) + 1
            );
        }


        // -----------------------------------------
        // Find highest vote target
        // -----------------------------------------

        int highestVotes = 0;
        Long eliminatedUserId = null;
        boolean tie = false;


        for (Map.Entry<Long, Integer> entry :
                voteCounts.entrySet()) {

            int votes = entry.getValue();

            if (votes > highestVotes) {

                highestVotes = votes;
                eliminatedUserId = entry.getKey();
                tie = false;

            } else if (votes == highestVotes) {

                tie = true;
            }
        }


        // -----------------------------------------
        // Eliminate player if there is no tie
        // -----------------------------------------

        if (!tie && eliminatedUserId != null) {

            GamePlayer eliminated =
                    gameSession.getPlayers()
                            .get(eliminatedUserId);

            if (eliminated != null) {

                eliminated.setAlive(false);
            }

            gameSession.setEliminatedUserId(
                    eliminatedUserId
            );


            // Notify all players.
            gameEventService.publish(
                    gameSession,
                    GameEventType.PLAYER_ELIMINATED,
                    eliminatedUserId
            );
        }


        // -----------------------------------------
        // Notify that voting has resolved
        // -----------------------------------------

        gameEventService.publish(
                gameSession,
                GameEventType.VOTING_RESOLVED,
                eliminatedUserId
        );


        // -----------------------------------------
        // Clear votes
        // -----------------------------------------

        gameSession.getVotes().clear();


        // -----------------------------------------
        // Check win condition
        // -----------------------------------------

        boolean gameOver =
                winConditionService.checkWinCondition(gameSession);


        if (gameOver) {

            gameEventService.publish(
                    gameSession,
                    GameEventType.GAME_OVER,
                    gameSession.getGameResult()
            );

            return;
        }


        // -----------------------------------------
        // No winner → start next night
        // -----------------------------------------

        gameSession.startNextNight();


        // Notify all clients.
        gameEventService.publish(
                gameSession,
                GameEventType.NIGHT_STARTED,
                gameSession.getRoundNumber()
        );
    }
}