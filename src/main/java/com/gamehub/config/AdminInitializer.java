package com.gamehub.config;

import com.gamehub.domain.user.User;
import com.gamehub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        @Value("${admin.username}")
        private String adminUsername;

        @Value("${admin.email}")
        private String adminEmail;

        @Value("${admin.password}")
        private String adminPassword;

        @Value("${admin.displayName}")
        private String adminDisplayName;

        public AdminInitializer(
                UserRepository userRepository,
                PasswordEncoder passwordEncoder
        ) {
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
        }

        @Bean
        public CommandLineRunner initializeAdmin() {
            return new CommandLineRunner() {
                @Override
                public void run(String[] args) {
                    if(!userRepository.existsByRole(User.Role.ADMIN)){
                        String hashedPassword = passwordEncoder.encode(adminPassword);
                        User newUser = new User(
                                adminUsername,
                                adminEmail,
                                hashedPassword,
                                adminDisplayName,
                                User.Role.ADMIN);

                        userRepository.save(newUser);
                    }

                }
            };
        }
}
