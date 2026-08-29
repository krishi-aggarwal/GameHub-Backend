package com.gamehub.room.exception;

public class PlayerAlreadyInRoomException extends RuntimeException{
    public PlayerAlreadyInRoomException(String msg){
        super(msg);
    }
}
