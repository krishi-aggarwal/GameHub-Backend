
        package com.gamehub.auth.service;

import com.gamehub.auth.dto.*;
import com.gamehub.auth.exception.EmailExistsException;
import com.gamehub.auth.exception.InvalidCredentialsException;
import com.gamehub.auth.exception.UsernameExistsException;
import com.gamehub.auth.security.JwtService;
import com.gamehub.domain.user.User;
import com.gamehub.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    // DI
    public final UserRepository userRepository;
    public final PasswordEncoder passwordEncoder;
    public final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse registerUser(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameExistsException(
                    "Username already Exists!"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailExistsException(
                    "Email already Exists!"
            );
        }

        // Password hashing
        String hashedPassword =
                passwordEncoder.encode(
                        request.getPassword()
                );

        // New User
        User newUser = new User(
                request.getUsername(),
                request.getEmail(),
                hashedPassword,
                request.getDisplayName(),
                User.Role.USER
        );

        User savedUser =
                userRepository.save(newUser);

        return new RegisterResponse(
                savedUser.getUserId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getDisplayName(),
                savedUser.getRole(),
                savedUser.getCreatedAt()
        );
    }

    public LoginResponse loginUser(LoginRequest request) {

        Optional<User> userOptional =
                userRepository.findByUsername(
                        request.getUsername()
                );

        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException(
                    "Invalid Credentials"
            );
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHashForAuthentication()
        )) {
            throw new InvalidCredentialsException(
                    "Invalid Username or password"
            );
        }

        String token =
                jwtService.generateToken(user);

        UserResponse userResponse =
                new UserResponse(
                        user.getUsername(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getAvatarUrl(),
                        user.getRole()
                );

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpiration(),
                userResponse
        );
    }
}

