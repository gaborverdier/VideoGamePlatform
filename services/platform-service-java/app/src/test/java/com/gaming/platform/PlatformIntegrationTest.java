package com.gaming.platform;

import com.gaming.api.requests.UserRegistrationRequest;
import com.gaming.api.models.UserModel;
import com.gaming.platform.repository.UserRepository;
import com.gaming.platform.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PlatformServiceIntegrationTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testUserRegistration() {
        UserRegistrationRequest request = UserRegistrationRequest.newBuilder()
                .setUsername("testuser")
                .setEmail("test@example.com")
                .setPassword("password123")
                .setCountry("USA")
                .build();

        UserModel user = userService.registerUser(request);

        assertNotNull(user.getUserId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());

        assertTrue(userRepository.existsByUsername("testuser"));
    }
    
    @Test
    void testDuplicateUsernameThrowsException() {
        UserRegistrationRequest request1 = UserRegistrationRequest.newBuilder()
                .setUsername("duplicate")
                .setEmail("user1@example.com")
                .setPassword("password123")
                .build();

        userService.registerUser(request1);

        UserRegistrationRequest request2 = UserRegistrationRequest.newBuilder()
                .setUsername("duplicate")
                .setEmail("user2@example.com")
                .setPassword("password123")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(request2);
        });
    }
}
