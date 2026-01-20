package com.gaming.platform.controller;

import com.gaming.platform.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody Map<String, String> loginRequest,
            HttpServletRequest request) {

        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // TODO: Implement actual authentication
        // For now, just track login

        return userService.getUserByUsername(username)
                .map(user -> {
                    String ipAddress = request.getRemoteAddr();
                    userService.updateLastLogin(user.getUserId(), ipAddress);

                    return ResponseEntity.ok(Map.of(
                            "message", "Login successful",
                            "userId", user.getUserId(),
                            "username", user.getUsername()));
                })
                .orElse(ResponseEntity.status(401)
                        .body(Map.of("error", "Invalid credentials")));
    }
}