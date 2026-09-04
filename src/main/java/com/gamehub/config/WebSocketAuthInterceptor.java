package com.gamehub.config;

import com.gamehub.auth.security.JwtService;
import com.gamehub.domain.user.User;
import com.gamehub.repository.UserRepository;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor
        implements ChannelInterceptor {

    private final JwtService jwtService;

    private final UserRepository userRepository;

    public WebSocketAuthInterceptor(
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            return message;
        }

        String authorization =
                accessor.getFirstNativeHeader(
                        "Authorization"
                );

        /*
         * Authenticate whenever the client sends
         * an Authorization header.
         *
         * This works for CONNECT as well as SEND.
         */
        if (
                authorization != null &&
                        authorization.startsWith("Bearer ")
        ) {

            String token =
                    authorization.substring(7);

            Authentication authentication =
                    authenticate(token);

            if (authentication != null) {

                accessor.setUser(
                        authentication
                );

                System.out.println(
                        "WebSocket authenticated: "
                                + authentication.getName()
                );
            }
        }

        /*
         * If there is no Authorization header,
         * preserve the already authenticated user.
         */
        if (
                accessor.getUser() == null &&
                        accessor.getSessionAttributes() != null
        ) {

            Object authentication =
                    accessor
                            .getSessionAttributes()
                            .get(
                                    "webSocketAuthentication"
                            );

            if (
                    authentication
                            instanceof Authentication
            ) {

                accessor.setUser(
                        (Authentication) authentication
                );
            }
        }

        /*
         * Store authentication in session
         * so later messages can reuse it.
         */
        if (
                accessor.getUser()
                        instanceof Authentication authentication
                        &&
                        accessor.getSessionAttributes() != null
        ) {

            accessor
                    .getSessionAttributes()
                    .put(
                            "webSocketAuthentication",
                            authentication
                    );
        }

        if (
                StompCommand.CONNECT.equals(
                        accessor.getCommand()
                )
        ) {

            if (accessor.getUser() != null) {

                System.out.println(
                        "WebSocket CONNECT authenticated: "
                                + accessor
                                .getUser()
                                .getName()
                );

            } else {

                System.out.println(
                        "WebSocket CONNECT without authentication"
                );
            }
        }

        return message;
    }

    private Authentication authenticate(
            String token
    ) {

        try {

            String username =
                    jwtService.extractUsername(
                            token
                    );

            if (username == null) {
                return null;
            }

            User user =
                    userRepository
                            .findByUsername(username)
                            .orElse(null);

            if (user == null) {
                return null;
            }

            if (
                    !jwtService.isTokenValid(
                            token,
                            user
                    )
            ) {

                return null;
            }

            return new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    List.of(
                            new SimpleGrantedAuthority(
                                    "ROLE_"
                                            + user
                                            .getRole()
                                            .name()
                            )
                    )
            );

        } catch (Exception exception) {

            System.out.println(
                    "WebSocket JWT authentication failed: "
                            + exception.getMessage()
            );

            return null;
        }
    }
}