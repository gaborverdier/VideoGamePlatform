package com.gaming.platform.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaming.api.models.UserModel;
import com.gaming.api.requests.UserRegistrationRequest;
import com.gaming.platform.model.User;
import com.gaming.platform.producer.EventProducer;
import com.gaming.platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final EventProducer eventProducer;

    @Transactional
    public UserModel registerUser(UserRegistrationRequest request) {

        if (userRepository.existsByUsername(request.getUsername().toString())) {
            log.warn("Attempt to register with existing username: {}", request.getUsername());
            throw new IllegalArgumentException("Username already exists:  " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail().toString())) {
            log.warn("Attempt to register with existing email: {}", request.getEmail());
            throw new IllegalArgumentException("Email already exists:  " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername().toString());
        user.setEmail(request.getEmail().toString());
        user.setPassword(request.getPassword().toString()); // TODO: Hash password before saving
        user.setCountry(request.getCountry() != null ? request.getCountry().toString() : null);
        user.setRegistrationDate(LocalDateTime.now());
        user.setLastLogin(java.time.LocalDateTime.now());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        eventProducer.publishUserRegistered(savedUser);

        log.info("Registered new user: {} (ID: {})", savedUser.getUsername(), savedUser.getUserId());

        return toUserResponse(savedUser);
    }

    public List<UserModel> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public Optional<UserModel> getUserById(String userId) {
        return userRepository.findById(userId)
                .map(this::toUserResponse);
    }

    public Optional<UserModel> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::toUserResponse);
    }

    public Optional<UserModel> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::toUserResponse);
    }

    @Transactional
    public void updateLastLogin(String userId, String ipAddress) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Publish user-login event to Kafka
            eventProducer.publishUserLogin(user, ipAddress);

            log.info("Updated last login for user: {} from IP: {}", user.getUsername(), ipAddress);
        });
    }

    private UserModel toUserResponse(User user) {
        return UserModel.newBuilder()
                .setUserId(user.getUserId() != null ? user.getUserId() : "")
                .setUsername(user.getUsername() != null ? user.getUsername() : "")
                .setEmail(user.getEmail() != null ? user.getEmail() : "")
                .setRegisteredAt(
                        user.getRegistrationDate() != null
                                ? user.getRegistrationDate().toInstant(ZoneOffset.UTC).toEpochMilli()
                                : System.currentTimeMillis())
                .setLastLogin(
                        user.getLastLogin() != null
                                ? user.getLastLogin().toInstant(ZoneOffset.UTC).toEpochMilli()
                                : null)
                .setCountry(user.getCountry())
                .build();
    }
}