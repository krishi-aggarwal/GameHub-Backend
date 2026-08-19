package com.gamehub.auth.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @Size(max=25)
    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 8)
    private String password;

    public LoginRequest(String username , String password){
        this.username=username;
        this.password=password;
    }

    public LoginRequest(){

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
