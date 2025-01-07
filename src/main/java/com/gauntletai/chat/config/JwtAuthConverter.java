package com.gauntletai.chat.config;

import com.gauntletai.chat.domain.User;
import com.gauntletai.chat.domain.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UserService userService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Get or create user in our database
        User user = userService.getOrCreateUser(jwt);
        
        // Create authentication token with user details
        return new JwtAuthenticationToken(
            jwt,
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            user.getId() // Use this as the principal name
        );
    }
} 