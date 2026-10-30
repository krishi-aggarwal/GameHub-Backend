package com.gamehub.game.fishpond.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class FishpondDtos {

    private FishpondDtos() {
    }

    public record StartResponse(UUID sessionId) {
    }

    public record SubmissionRequest(String message) {
    }

    public record NavigationRequest(String action) {
    }

    public record ChatRequest(String message) {
    }

    public record PlayerState(
            Long userId,
            String displayName,
            boolean submitted,
            boolean left
    ) {
    }

    public record MessageState(
            UUID id,
            String message,
            String authorName
    ) {
    }

    public record ChatState(
            UUID id,
            Long userId,
            String displayName,
            String message,
            Instant sentAt
    ) {
    }

    public record GameState(
            UUID sessionId,
            String phase,
            String hostUsername,
            boolean isHost,
            boolean hasSubmitted,
            int totalPlayers,
            int submittedCount,
            int currentIndex,
            MessageState currentMessage,
            boolean canGoBack,
            boolean canGoNext,
            boolean canRevealAuthors,
            boolean authorsRevealed,
            List<PlayerState> players,
            List<MessageState> messages,
            List<ChatState> chatMessages
    ) {
    }
}