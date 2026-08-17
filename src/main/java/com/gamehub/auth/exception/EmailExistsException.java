package com.gamehub.auth.exception;

public class EmailExistsException extends RuntimeException{
    public EmailExistsException(String msg){
        super(msg);
    }
}
