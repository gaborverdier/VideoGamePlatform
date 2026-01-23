package com.gaming.platform.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaming.api.models.UserModel;
import com.gaming.platform.repository.UserRepository;
import com.gaming.platform.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody Map<String, String> loginRequest,
            HttpServletRequest request) {

        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // Find user by username (User entity)
        return userRepository.findByUsername(username)
                .map(user -> {
                    // Check password (plain text for now, hash in production)
                    if (user.getPassword().equals(password)) {
                        String ipAddress = request.getRemoteAddr();
                        // Update last login
                        // You may need to inject UserService here if not present
                        // For now, update directly
                        user.setLastLogin(java.time.LocalDateTime.now());
                        userRepository.save(user);
                        System.out.println("User " + username + " logged in from IP: " + ipAddress);
                        return ResponseEntity.ok(Map.of(
                                "message", "Login successful",
                                "userId", user.getUserId(),
                                "username", user.getUsername()
                        ));
                    } else {
                        return ResponseEntity.status(401)
                                .body(Map.of("error", "Invalid credentials"));
                    }
                })
                .orElse(ResponseEntity.status(401)
                        .body(Map.of("error", "Invalid credentials")));
    }
}