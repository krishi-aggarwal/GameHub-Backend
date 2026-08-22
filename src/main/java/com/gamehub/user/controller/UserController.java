package com.gamehub.user.controller;

import com.gamehub.domain.user.User;
import com.gamehub.user.dto.UserProfileResponse;
import com.gamehub.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService=userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.OK).body(userService.getCurrentUserProfile(user.getUsername()));

    }
}
