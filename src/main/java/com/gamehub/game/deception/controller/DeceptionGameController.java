package com.gamehub.game.deception.controller;

import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.GameSession;
import com.gamehub.game.deception.domain.NightAction;
import com.gamehub.game.deception.dto.*;
import com.gamehub.game.deception.service.*;
import com.gamehub.game.exception.GameSessionNotFoundException;
import com.gamehub.mainDto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/deception/rooms")
public class DeceptionGameController {

    private final DeceptionGameService deceptionGameService;
    private final GameSessionRegistry gameSessionRegistry;
    private final NightActionService nightActionService;
    private final VotingService votingService;

    public DeceptionGameController(
            DeceptionGameService deceptionGameService,
            GameSessionRegistry gameSessionRegistry,
            NightActionService nightActionService,
            VotingService votingService
    ) {
        this.deceptionGameService = deceptionGameService;
        this.gameSessionRegistry = gameSessionRegistry;
        this.nightActionService = nightActionService;
        this.votingService = votingService;
    }


    // =========================================================
    // START GAME
    // =========================================================

    @PostMapping("/{roomCode}/start")
    public ResponseEntity<ApiResponse<StartGameResponse>> startGame(
            @PathVariable String roomCode,
            Authentication authentication,
            @RequestBody StartGameRequest request
    ) {

        User user =
                (User) authentication.getPrincipal();

        StartGameResponse response =
                deceptionGameService.startGame(
                        roomCode,
                        user,
                        request
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Game Started",
                        response
                )
        );
    }


    // =========================================================
    // GAME STATE
    // =========================================================

    @GetMapping("/{sessionId}/state")
    public ResponseEntity<ApiResponse<GameStateResponse>> getGameState(
            @PathVariable UUID sessionId,
            Authentication authentication
    ) {

        User user =
                (User) authentication.getPrincipal();

        GameStateResponse response =
                deceptionGameService.getGameState(
                        sessionId,
                        user
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Game State",
                        response
                )
        );
    }

    @GetMapping("/{roomCode}/active-session")
    public ResponseEntity<ApiResponse<StartGameResponse>> getActiveSession(
            @PathVariable String roomCode,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        StartGameResponse response =
                deceptionGameService.getActiveSession(roomCode, user);

        return ResponseEntity.ok(
                new ApiResponse<>("Active game session", response)
        );
    }


    // =========================================================
    // NIGHT ACTION
    // =========================================================

    @PostMapping("/{sessionId}/night/{action}")
    public ResponseEntity<ApiResponse<String>> performNightAction(
            @PathVariable UUID sessionId,
            @PathVariable NightAction action,
            Authentication authentication,
            @RequestBody NightActionRequest request
    ) {

        User user =
                (User) authentication.getPrincipal();

        GameSession gameSession =
                gameSessionRegistry.retrieveSession(
                        sessionId
                );

        if (gameSession == null) {

            throw new GameSessionNotFoundException(
                    "Game Session not found"
            );
        }

        String result =
                nightActionService.performAction(
                        gameSession,
                        user,
                        action,
                        request
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        result,
                        "SUCCESS"
                )
        );
    }


    // =========================================================
    // START VOTING
    // =========================================================

    @PostMapping("/{sessionId}/start-voting")
    public ResponseEntity<ApiResponse<String>> startVoting(
            @PathVariable UUID sessionId,
            Authentication authentication
    ) {

        User user =
                (User) authentication.getPrincipal();

        GameSession gameSession =
                gameSessionRegistry.retrieveSession(
                        sessionId
                );

        if (gameSession == null) {

            throw new GameSessionNotFoundException(
                    "Game Session not found"
            );
        }

        /*
         * VotingService is now responsible for:
         *
         * 1. Checking that the game is in DAY
         * 2. Checking that the user is the host
         * 3. Changing DAY → VOTING
         * 4. Publishing VOTING_STARTED
         */
        String result =
                votingService.startVoting(
                        gameSession,
                        user
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        result,
                        "SUCCESS"
                )
        );
    }


    // =========================================================
    // VOTE
    // =========================================================

    @PostMapping("/{sessionId}/vote")
    public ResponseEntity<ApiResponse<String>> vote(
            @PathVariable UUID sessionId,
            Authentication authentication,
            @RequestBody VoteRequest request
    ) {

        User user =
                (User) authentication.getPrincipal();

        GameSession gameSession =
                gameSessionRegistry.retrieveSession(
                        sessionId
                );

        if (gameSession == null) {

            throw new GameSessionNotFoundException(
                    "Game Session not found"
            );
        }

        String result =
                votingService.castVote(
                        gameSession,
                        user,
                        request
                );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        result,
                        "SUCCESS"
                )
        );
    }


    // =========================================================
    // GAME RESULT
    // =========================================================

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<ApiResponse<GameResultResponse>> getGameResult(
            @PathVariable UUID sessionId
    ) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Game Result",
                        deceptionGameService.getGameResult(
                                sessionId
                        )
                )
        );
    }


    // =========================================================
    // DAY STATE
    // =========================================================

    @GetMapping("/{sessionId}/day")
    public ResponseEntity<ApiResponse<DayStateResponse>> getDayState(
            @PathVariable UUID sessionId
    ) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Day State",
                        deceptionGameService.getDayState(
                                sessionId
                        )
                )
        );
    }


    // =========================================================
    // INVESTIGATION RESULT
    // =========================================================

    @GetMapping("/{sessionId}/investigation")
    public ResponseEntity<ApiResponse<InvestigationResponse>> getInvestigationResult(
            @PathVariable UUID sessionId,
            Authentication authentication
    ) {

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Investigation Result",
                        deceptionGameService.getInvestigationResult(
                                sessionId,
                                user
                        )
                )
        );
    }
}