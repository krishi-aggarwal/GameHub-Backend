package com.gamehub.game.exception;

public class GameRuleViolationException extends RuntimeException {

    public GameRuleViolationException(String message) {
        super(message);
    }
}