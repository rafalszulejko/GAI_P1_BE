package com.gauntletai.chat.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class SecurityUtils {
    private SecurityUtils() {
        // Utility class
    }

    public static String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public static JwtAuthenticationToken getCurrentAuthentication() {
        return (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }
} 