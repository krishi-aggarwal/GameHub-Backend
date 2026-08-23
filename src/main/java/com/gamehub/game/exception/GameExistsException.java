package com.gamehub.game.exception;

public class GameExistsException extends RuntimeException{
    public GameExistsException(String msg){
        super(msg);
    }
}
