package com.gamehub.Auth.exception;

public class EmailExistsException extends RuntimeException{
    public EmailExistsException(String msg){
        super(msg);
    }
}
