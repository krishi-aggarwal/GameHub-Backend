package com.gamehub.game.deception.controller;

import com.gamehub.domain.user.User;
import com.gamehub.game.deception.dto.ChatMessageRequest;
import com.gamehub.game.deception.service.GameChatService;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class GameChatController {

    private final GameChatService gameChatService;

    public GameChatController(
            GameChatService gameChatService
    ) {
        this.gameChatService = gameChatService;
    }

    @MessageMapping("/game/{sessionId}/chat")
    public void sendMessage(
            @DestinationVariable UUID sessionId,
            ChatMessageRequest request,
            Principal principal
    ) {

        if (principal == null) {

            throw new IllegalStateException(
                    "WebSocket user is not authenticated."
            );
        }

        if (!(principal instanceof Authentication authentication)) {

            throw new IllegalStateException(
                    "Invalid WebSocket authentication."
            );
        }

        Object principalObject =
                authentication.getPrincipal();

        if (!(principalObject instanceof User user)) {

            throw new IllegalStateException(
                    "Invalid authenticated user."
            );
        }

        gameChatService.sendMessage(
                sessionId,
                user,
                request
        );
    }
}