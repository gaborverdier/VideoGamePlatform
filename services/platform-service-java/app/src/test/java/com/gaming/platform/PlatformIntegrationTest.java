package com.gaming.platform;

import com.gaming.platform.dto.UserRegistrationDTO;
import com.gaming.platform.model.User;
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
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setCountry("USA");
        
        User user = userService.registerUser(dto);
        
        assertNotNull(user.getUserId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.getActive());
        
        assertTrue(userRepository.existsByUsername("testuser"));
    }
    
    @Test
    void testDuplicateUsernameThrowsException() {
        UserRegistrationDTO dto1 = new UserRegistrationDTO();
        dto1.setUsername("duplicate");
        dto1.setEmail("user1@example.com");
        dto1.setPassword("password123");
        
        userService.registerUser(dto1);
        
        UserRegistrationDTO dto2 = new UserRegistrationDTO();
        dto2.setUsername("duplicate");
        dto2.setEmail("user2@example.com");
        dto2.setPassword("password123");
        
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(dto2);
        });
    }
}
