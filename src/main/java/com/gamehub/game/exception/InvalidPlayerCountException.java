package com.gamehub.game.exception;

public class InvalidPlayerCountException extends RuntimeException{
    public InvalidPlayerCountException(String msg){
        super(msg);
    }
}
