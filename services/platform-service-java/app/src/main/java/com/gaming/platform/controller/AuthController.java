package com.gaming.platform.controller;

import java.util.Map;
import java.util.Objects;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaming.api.models.UserModel;
import com.gaming.platform.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private static ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
    }

    private static ResponseEntity<Map<String, String>> badRequest(String msg) {
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, HttpServletRequest request) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.warn("Login attempt with missing username or password");
            return badRequest("Missing username or password");
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    // Check password (plain text for now, hash in production)
                    if (Objects.equals(user.getPassword(), password)) {
                        String ipAddress = request.getRemoteAddr();
                        // Update last login
                        user.setLastLogin(LocalDateTime.now());
                        userRepository.save(user);
                        log.info("User {} logged in from IP: {}", username, ipAddress);

                        UserModel userModel = new UserModel();
                        userModel.setUserId(user.getUserId());
                        userModel.setUsername(user.getUsername());
                        userModel.setEmail(user.getEmail());
                        userModel.setBalance(user.getBalance());

                        Long registeredAt = toEpochMillis(user.getRegistrationDate());
                        Long lastLoginAt = toEpochMillis(user.getLastLogin());

                        userModel.setRegisteredAt(registeredAt);
                        userModel.setLastLogin(lastLoginAt);
                        userModel.setCountry(user.getCountry());

                        return ResponseEntity.ok(userModel);
                    }
                    return unauthorized();
                })
                .orElseGet(AuthController::unauthorized);
    }

    private static Long toEpochMillis(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.atZone(java.time.ZoneOffset.UTC).toInstant().toEpochMilli();
    }
}