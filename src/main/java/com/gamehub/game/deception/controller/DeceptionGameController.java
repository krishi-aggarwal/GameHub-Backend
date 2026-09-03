package com.gamehub.game.deception.controller;

import com.gamehub.domain.user.User;
import com.gamehub.game.deception.dto.StartGameRequest;
import com.gamehub.game.deception.dto.StartGameResponse;
import com.gamehub.game.deception.service.DeceptionGameService;
import com.gamehub.mainDto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deception/rooms")
public class DeceptionGameController {
    private final DeceptionGameService deceptionGameService;

    public DeceptionGameController(DeceptionGameService deceptionGameService){
        this.deceptionGameService = deceptionGameService;
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


}
