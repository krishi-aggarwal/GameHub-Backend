package com.gamehub.auth.controller;

import com.gamehub.auth.dto.LoginRequest;
import com.gamehub.auth.dto.LoginResponse;
import com.gamehub.auth.dto.RegisterRequest;
import com.gamehub.auth.dto.RegisterResponse;
import com.gamehub.auth.service.AuthService;
import com.gamehub.mainDto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @Valid @RequestBody RegisterRequest request
            ){
        RegisterResponse response = authService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(
                        "Registration Successfull!",
                        response
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Valid @RequestBody LoginRequest request
            ){
        LoginResponse response = authService.loginUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(
                        "Login Successfull!",
                        response
                ));
    }
}
