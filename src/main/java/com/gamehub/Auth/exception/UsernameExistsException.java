package com.gamehub.Auth.exception;

public class UsernameExistsException extends RuntimeException{
    public UsernameExistsException(String msg){
        super(msg);
    }
}
