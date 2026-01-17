package com.gaming.platform.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.gaming.platform.dto.UserRegistrationDTO;
import com.gaming.platform.model.User;
import com.gaming.platform.producer.EventProducer;
import com.gaming.platform.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final EventProducer eventProducer;

    @Transactional
    public User registerUser(UserRegistrationDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            log.warn("Attempt to register with existing username: {}", dto.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Attempt to register with existing email: {}", dto.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // TODO : Hash password before saving
        user.setCountry(dto.getCountry());
        user.setRegistrationDate(LocalDateTime.now());
        user.setActive(true);

        User savedUser = userRepository.save(user);
        log.info("Registered new user: {}", savedUser.getUsername());

        return savedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void updateLastLogin(String userId, String ipAddress){
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            eventProducer.publishUserLogin(user, ipAddress);

            log.info("Updated last login for user: {}", user.getUsername());
        });
    }

}
