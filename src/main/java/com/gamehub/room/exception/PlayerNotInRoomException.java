package com.gamehub.room.exception;

public class PlayerNotInRoomException extends  RuntimeException{
    public PlayerNotInRoomException(String msg){
        super(msg);
    }
}
