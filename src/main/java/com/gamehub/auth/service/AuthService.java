package com.gamehub.auth.service;

import com.gamehub.auth.dto.RegisterRequest;
import com.gamehub.auth.dto.RegisterResponse;
import com.gamehub.auth.exception.EmailExistsException;
import com.gamehub.auth.exception.UsernameExistsException;
import com.gamehub.domain.user.User;
import com.gamehub.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    //DI
    public final UserRepository userRepository;
    public final PasswordEncoder passwordEncoder;
    //public final RegisterRequest registerRequest;

    public AuthService(UserRepository userRepository , PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder=passwordEncoder;
    }

    public RegisterResponse registerUser(RegisterRequest request){

        if(userRepository.existsByUsername(request.getUsername())){
            throw new UsernameExistsException(("Username already Exists!"));
        }


        if(userRepository.existsByEmail((request.getEmail()))) {
            throw new EmailExistsException(("Email already Exists!"));
        }

        //password hashing
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        //new User
        User newUser = new User(request.getUsername(),request.getEmail(),hashedPassword,request.getDisplayName(),User.Role.USER);

        User savedUser = userRepository.save(newUser);

        return new RegisterResponse(
                savedUser.getUserId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getDisplayName(),
                savedUser.getRole() ,
                savedUser.getCreatedAt()
        );
    }
}
