package com.gamehub.game.exception;

public class InsufficientPlayersException extends RuntimeException{
    public InsufficientPlayersException(String msg){
        super(msg);
    }
}
