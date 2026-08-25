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
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        // BCrypt is used to securely hash user passwords.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

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

                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/games")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/games/**")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/games/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/error").permitAll()

                        .anyRequest().authenticated()
                )



                // If a protected endpoint is accessed without
                // valid authentication, Spring Security calls
                // this AuthenticationEntryPoint.
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                authenticationEntryPoint()
                        ).accessDeniedHandler(accessDeniedHandler())
                )

                // Run our JWT filter before Spring's
                // UsernamePasswordAuthenticationFilter.
                //
                // The JWT filter extracts and validates the token
                // and places the authenticated User into
                // SecurityContext.
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, authException) -> {

            // Tell the client that authentication is required.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            // Tell the client that we are returning JSON.
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            // Build our standard API error response.
            ErrorResponse errorResponse = new ErrorResponse(
                    "Unauthorized",
                    401,
                    request.getRequestURI(),
                    "Authentication is required to access this resource."
            );

            // Convert ErrorResponse object into JSON.
            ObjectMapper objectMapper = new ObjectMapper();

            response.getWriter().write(
                    objectMapper.writeValueAsString(errorResponse)
            );
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {

        return (request, response, accessDeniedException) -> {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse errorResponse = new ErrorResponse(
                    "Forbidden",
                    403,
                    request.getRequestURI(),
                    "You do not have permission to access this resource."
            );

            ObjectMapper objectMapper = new ObjectMapper();

            response.getWriter().write(
                    objectMapper.writeValueAsString(errorResponse)
            );
        };
    }
}