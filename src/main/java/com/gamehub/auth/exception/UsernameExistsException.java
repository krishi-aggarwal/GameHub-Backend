package com.gamehub.auth.exception;

public class UsernameExistsException extends RuntimeException{
    public UsernameExistsException(String msg){
        super(msg);
    }
}
