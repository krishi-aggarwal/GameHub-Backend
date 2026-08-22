package com.gamehub.user.controller;

import com.gamehub.domain.user.User;
import com.gamehub.mainDto.ApiResponse;
import com.gamehub.user.dto.UpdateProfileRequest;
import com.gamehub.user.dto.UserProfileResponse;
import com.gamehub.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService=userService;
    }


    //Get User Profile
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.OK).body(userService.getCurrentUserProfile(user.getUsername()));

    }


    //Update User Profile
    @PutMapping("profile")
    public ResponseEntity<ApiResponse> updateUserProfile(
            @Valid @RequestBody
            UpdateProfileRequest request,
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Profile updated successfully.",
                        userService.updateProfile(user.getUsername(), request)
                )
        );
    }
}
