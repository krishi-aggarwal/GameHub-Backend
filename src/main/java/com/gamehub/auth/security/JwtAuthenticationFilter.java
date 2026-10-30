//package com.gamehub.auth.security;
//
//import com.gamehub.domain.user.User;
//import com.gamehub.repository.UserRepository;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Optional;
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JwtService jwtService;
//    private final UserRepository userRepository;
//
//    public JwtAuthenticationFilter(
//            JwtService jwtService,
//            UserRepository userRepository
//    ) {
//        this.jwtService = jwtService;
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain
//    ) throws ServletException, IOException {
//
//
//        //getting token
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String token = authHeader.substring(7);
//
//        try {
//
//            String username = jwtService.extractUsername(token);
//
//
//            Optional<User> userOptional =
//                    userRepository.findByUsername(username);
//
//            if (userOptional.isEmpty()) {
//
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            User user = userOptional.get();
//
//
//
//            boolean valid = jwtService.isTokenValid(token, user);
//
//            if (!valid) {
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//
//            UsernamePasswordAuthenticationToken authentication =
//                    new UsernamePasswordAuthenticationToken(
//                            user,
//                            null,
//                            List.of(
//                                    new SimpleGrantedAuthority(
//                                            "ROLE_" + user.getRole().name()
//                                    )
//                            )
//                    );
//
//            SecurityContextHolder.getContext()
//                    .setAuthentication(authentication);
//
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}

package com.gamehub.auth.security;

import com.gamehub.domain.user.User;
import com.gamehub.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
                String path = request.getServletPath();
                return "/api/auth/login".equals(path)
                                || "/api/auth/register".equals(path);
        }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Get Authorization header
        String authHeader = request.getHeader("Authorization");

        // No JWT → continue normally
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Remove "Bearer "
        String token = authHeader.substring(7);

        try {

            // Extract username from JWT
            String username = jwtService.extractUsername(token);

            // Find user
            Optional<User> userOptional =
                    userRepository.findByUsername(username);

            // User not found
            if (!userOptional.isPresent()) {
                filterChain.doFilter(request, response);
                return;
            }

            User user = userOptional.get();

            // Validate JWT
            boolean valid =
                    jwtService.isTokenValid(token, user);

            if (!valid) {
                filterChain.doFilter(request, response);
                return;
            }

            // Create Spring Security authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.singletonList(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + user.getRole().name()
                                    )
                            )
                    );

            // Store authentication
            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

                } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        // Continue request
        filterChain.doFilter(request, response);
    }
}