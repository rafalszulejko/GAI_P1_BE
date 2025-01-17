package com.gauntletai.chat.domain;

import com.gauntletai.chat.domain.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final SseService sseService;
    @GetMapping("/me")
    public User getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return userService.getOrCreateUser(jwt);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) {
        return userService.findById(id);
    }

    @PostMapping("/heartbeat")
    public void heartbeat() {
        sseService.updatePresence();
    }

    @GetMapping("/online")
    public Set<String> getOnlineUsers() {
        return sseService.getOnlineUsers();
    }
}