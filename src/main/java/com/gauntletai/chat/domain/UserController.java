package com.gauntletai.chat.domain;

import com.gauntletai.chat.domain.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public User getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return userService.getOrCreateUser(jwt);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) {
        return userService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(User.class, id));
    }

    @PutMapping("/me/username")
    public User updateUsername(@AuthenticationPrincipal Jwt jwt, @RequestBody UpdateUsernameRequest request) {
        User user = userService.getOrCreateUser(jwt);
        user.setUsername(request.getUsername());
        return userService.save(user);
    }
}

record UpdateUsernameRequest(String username) {
    public String getUsername() {
        return username;
    }
} 