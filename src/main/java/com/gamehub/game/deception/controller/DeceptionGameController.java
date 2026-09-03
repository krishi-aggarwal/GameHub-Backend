package com.gamehub.game.deception.controller;

import com.gamehub.domain.user.User;
import com.gamehub.game.deception.domain.GameEventType;
import com.gamehub.game.deception.domain.GameSession;
import com.gamehub.game.deception.domain.NightAction;
import com.gamehub.game.deception.dto.*;
import com.gamehub.game.deception.service.*;
import com.gamehub.game.exception.GameSessionNotFoundException;
import com.gamehub.game.exception.UnauthorizedException;
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
    private final GameEventService gameEventService;

    public DeceptionGameController(
            DeceptionGameService deceptionGameService,
            GameSessionRegistry gameSessionRegistry,
            NightActionService nightActionService,
            VotingService votingService,
            GameEventService gameEventService
    ) {
        this.deceptionGameService = deceptionGameService;
        this.gameSessionRegistry = gameSessionRegistry;
        this.nightActionService = nightActionService;
        this.votingService = votingService;
        this.gameEventService = gameEventService;
    }

    @PostMapping("/{roomCode}/start")
    public ResponseEntity<ApiResponse<StartGameResponse>> startGame(
            @PathVariable String roomCode,
            Authentication authentication,
            @RequestBody StartGameRequest request
            ){
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(
                new ApiResponse(
                        "Game Started",
                        deceptionGameService.startGame(roomCode,user,request)
                )
        );
    }

    @GetMapping("/{sessionId}/state")
    public ResponseEntity<ApiResponse<GameStateResponse>> getGameState(
            @PathVariable UUID sessionId,
            Authentication authentication
    ) {

        // Get the currently authenticated user from JWT/Spring Security.
        User user = (User) authentication.getPrincipal();

        GameStateResponse response =
                deceptionGameService.getGameState(sessionId, user);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Game State",
                        response
                )
        );
    }

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
                gameSessionRegistry.retrieveSession(sessionId);

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

    @PostMapping("/{sessionId}/start-voting")
    public ResponseEntity<ApiResponse<String>> startVoting(
            @PathVariable UUID sessionId,
            Authentication authentication
    ) {

        // Get currently authenticated user.
        User user =
                (User) authentication.getPrincipal();


        // Retrieve the active game session.
        GameSession gameSession =
                gameSessionRegistry.retrieveSession(sessionId);


        // Session must exist.
        if (gameSession == null) {

            throw new GameSessionNotFoundException(
                    "Game Session not found"
            );
        }


        // Only the room host can start voting.
        if (!gameSession.getGameRoom()
                .getHost()
                .getUserId()
                .equals(user.getUserId())) {

            throw new UnauthorizedException(
                    "Only the host can start voting"
            );
        }


        // Change game phase:
        // DAY → VOTING
        gameSession.startVoting();


        // Notify all connected players.
        gameEventService.publish(
                gameSession,
                GameEventType.VOTING_STARTED,
                null
        );


        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Voting started",
                        "SUCCESS"
                )
        );
    }

    @PostMapping("/{sessionId}/vote")
    public ResponseEntity<ApiResponse<String>> vote(
            @PathVariable UUID sessionId,
            Authentication authentication,
            @RequestBody VoteRequest request
    ) {

        User user =
                (User) authentication.getPrincipal();

        GameSession gameSession =
                gameSessionRegistry.retrieveSession(sessionId);

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

    @GetMapping("/{sessionId}/day")
    public ResponseEntity<ApiResponse<DayStateResponse>> getDayState(
            @PathVariable UUID sessionId
    ) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Day State",
                        deceptionGameService.getDayState(sessionId)
                )
        );
    }

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
