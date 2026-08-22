package com.gamehub.user.service;


import com.gamehub.domain.user.User;
import com.gamehub.repository.UserRepository;
import com.gamehub.user.dto.UserProfileResponse;
import com.gamehub.user.exception.UserNotFoundException;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

     public UserService(UserRepository userRepository){
         this.userRepository=userRepository;
     }
    public UserProfileResponse getCurrentUserProfile(String username){
        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isEmpty()){
            throw new UserNotFoundException("User not Found!");
        }

        User user = userOptional.get();

        UserProfileResponse userProfileResponse = new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRole()
        );

        return userProfileResponse;
    }
}
