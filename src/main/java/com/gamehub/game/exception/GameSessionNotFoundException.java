package com.gamehub.game.exception;

public class GameSessionNotFoundException extends RuntimeException{
    public GameSessionNotFoundException(String message) {
        super(message);
    }
}
