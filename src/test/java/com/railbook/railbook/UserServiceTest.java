package com.railbook.railbook;

import com.railbook.railbook.dto.PasswordChangeDto;
import com.railbook.railbook.dto.UserProfileDto;
import com.railbook.railbook.dto.UserRegistrationDto;
import com.railbook.railbook.exception.BookingException;
import com.railbook.railbook.exception.UserAlreadyExistsException;
import com.railbook.railbook.model.Role;
import com.railbook.railbook.model.User;
import com.railbook.railbook.repository.UserRepository;
import com.railbook.railbook.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String testEmail = "testuser_" + System.currentTimeMillis() + "@railbook.com";

    @BeforeEach
    void setUp() {
        if (!userRepository.existsByEmail(testEmail)) {
            UserRegistrationDto dto = new UserRegistrationDto();
            dto.setFullName("Test Passenger");
            dto.setEmail(testEmail);
            dto.setPassword("secret123");
            dto.setConfirmPassword("secret123");
            dto.setPhone("9876543210");
            userService.registerUser(dto);
        }
    }

    @Test
    @DisplayName("Should successfully register a new user with BCrypt password")
    void testUserRegistration() {
        String uniqueEmail = "newuser_" + System.currentTimeMillis() + "@railbook.com";
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("Aarav Gupta");
        dto.setEmail(uniqueEmail);
        dto.setPassword("myPassword123");
        dto.setConfirmPassword("myPassword123");
        dto.setPhone("9123456780");

        User savedUser = userService.registerUser(dto);

        assertNotNull(savedUser.getId());
        assertEquals("Aarav Gupta", savedUser.getFullName());
        assertEquals(uniqueEmail, savedUser.getEmail());
        assertEquals(Role.ROLE_USER, savedUser.getRole());
        assertTrue(passwordEncoder.matches("myPassword123", savedUser.getPassword()));
        assertNotEquals("myPassword123", savedUser.getPassword());
    }

    @Test
    @DisplayName("Should reject registration when passwords do not match")
    void testRegistrationPasswordMismatch() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("Mismatch User");
        dto.setEmail("mismatch@railbook.com");
        dto.setPassword("pass1");
        dto.setConfirmPassword("pass2");

        assertThrows(BookingException.class, () -> userService.registerUser(dto));
    }

    @Test
    @DisplayName("Should prevent duplicate email registration")
    void testDuplicateEmailRejection() {
        UserRegistrationDto duplicateDto = new UserRegistrationDto();
        duplicateDto.setFullName("Duplicate User");
        duplicateDto.setEmail(testEmail);
        duplicateDto.setPassword("secret123");
        duplicateDto.setConfirmPassword("secret123");

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(duplicateDto));
    }

    @Test
    @DisplayName("Should retrieve and update user profile")
    void testUpdateProfile() {
        UserProfileDto profile = userService.getUserProfile(testEmail);
        assertEquals("Test Passenger", profile.getFullName());

        UserProfileDto updateDto = new UserProfileDto("Test Passenger Updated", testEmail, "9998887776");
        userService.updateProfile(testEmail, updateDto);

        User updatedUser = userService.findByEmail(testEmail);
        assertEquals("Test Passenger Updated", updatedUser.getFullName());
        assertEquals("9998887776", updatedUser.getPhone());
    }

    @Test
    @DisplayName("Should change user password successfully")
    void testChangePassword() {
        PasswordChangeDto pwdDto = new PasswordChangeDto();
        pwdDto.setCurrentPassword("secret123");
        pwdDto.setNewPassword("newSecret456");
        pwdDto.setConfirmNewPassword("newSecret456");

        userService.changePassword(testEmail, pwdDto);

        User user = userService.findByEmail(testEmail);
        assertTrue(passwordEncoder.matches("newSecret456", user.getPassword()));
    }
}
