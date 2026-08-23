package com.gamehub.game.exception;

public class GameNotExistsException extends RuntimeException{
    public GameNotExistsException(String msg){
        super(msg);
    }
}
