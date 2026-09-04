package com.gamehub.config;

import com.gamehub.auth.security.JwtAuthenticationFilter;
import com.gamehub.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        // BCrypt is used to securely hash user passwords.
        return new BCryptPasswordEncoder();
    }

    /*
     * CORS configuration.
     *
     * Expo Web runs on localhost:8081 during development.
     * Spring Boot runs on localhost:8080.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:8081",
                        "http://localhost:8080"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // Enable CORS for the REST API.
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                // We are building a stateless REST API,
                // so CSRF protection is disabled.
                .csrf(csrf -> csrf.disable())

                // We don't use server-side sessions.
                // Every request must authenticate using JWT.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Define which endpoints require authentication.
                .authorizeHttpRequests(auth -> auth

                        // Authentication endpoints are public.
                        .requestMatchers(
                                "/api/auth/**"
                        ).permitAll()

                        // Admin-only game management.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/games"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/games/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/games/**"
                        ).hasRole("ADMIN")

                        // Spring error endpoint.
                        .requestMatchers(
                                "/error"
                        ).permitAll()

                        // WebSocket handshake.
                        .requestMatchers(
                                "/ws/**"
                        ).permitAll()

                        // Everything else requires authentication.
                        .anyRequest().authenticated()
                )

                // Custom authentication/authorization errors.
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(
                                        authenticationEntryPoint()
                                )
                                .accessDeniedHandler(
                                        accessDeniedHandler()
                                )
                )

                // Run JWT filter before Spring Security's
                // UsernamePasswordAuthenticationFilter.
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, authException) -> {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE
            );

            ErrorResponse errorResponse =
                    new ErrorResponse(
                            "Unauthorized",
                            401,
                            request.getRequestURI(),
                            "Authentication is required to access this resource."
                    );

            ObjectMapper objectMapper =
                    new ObjectMapper();

            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            errorResponse
                    )
            );
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {

        return (request, response, accessDeniedException) -> {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );

            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE
            );

            ErrorResponse errorResponse =
                    new ErrorResponse(
                            "Forbidden",
                            403,
                            request.getRequestURI(),
                            "You do not have permission to access this resource."
                    );

            ObjectMapper objectMapper =
                    new ObjectMapper();

            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            errorResponse
                    )
            );
        };
    }
}