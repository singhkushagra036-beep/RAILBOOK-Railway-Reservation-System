package com.railbook.railbook.service.impl;

import com.railbook.railbook.dto.PasswordChangeDto;
import com.railbook.railbook.dto.UserProfileDto;
import com.railbook.railbook.dto.UserRegistrationDto;
import com.railbook.railbook.exception.BookingException;
import com.railbook.railbook.exception.ResourceNotFoundException;
import com.railbook.railbook.exception.UserAlreadyExistsException;
import com.railbook.railbook.model.Role;
import com.railbook.railbook.model.User;
import com.railbook.railbook.repository.UserRepository;
import com.railbook.railbook.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(UserRegistrationDto registrationDto) {
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new BookingException("Passwords do not match");
        }

        String normalizedEmail = registrationDto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("An account with email " + normalizedEmail + " already exists");
        }

        User user = new User(
                registrationDto.getFullName().trim(),
                normalizedEmail,
                passwordEncoder.encode(registrationDto.getPassword()),
                registrationDto.getPhone() != null ? registrationDto.getPhone().trim() : null,
                Role.ROLE_USER
        );

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        if (email == null) return null;
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String email) {
        User user = findByEmail(email);
        return new UserProfileDto(user.getFullName(), user.getEmail(), user.getPhone());
    }

    @Override
    public void updateProfile(String email, UserProfileDto profileDto) {
        User user = findByEmail(email);
        user.setFullName(profileDto.getFullName().trim());
        user.setPhone(profileDto.getPhone() != null ? profileDto.getPhone().trim() : null);
        userRepository.save(user);
    }

    @Override
    public void changePassword(String email, PasswordChangeDto passwordDto) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword())) {
            throw new BookingException("Current password is incorrect");
        }
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmNewPassword())) {
            throw new BookingException("New password confirmation does not match");
        }
        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepository.count();
    }

    @Override
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if ("admin@railbook.com".equalsIgnoreCase(user.getEmail())) {
            throw new BookingException("Primary system administrator account cannot be disabled");
        }
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    @Override
    public User toggleUserRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if ("admin@railbook.com".equalsIgnoreCase(user.getEmail())) {
            throw new BookingException("Primary system administrator role cannot be altered");
        }
        if (user.getRole() == Role.ROLE_ADMIN) {
            user.setRole(Role.ROLE_USER);
        } else {
            user.setRole(Role.ROLE_ADMIN);
        }
        return userRepository.save(user);
    }
}
