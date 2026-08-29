package com.gamehub.room.exception;

public class RoomFullException extends RuntimeException{
    public RoomFullException(String msg){
        super(msg);
    }
}
