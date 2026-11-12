package com.railbook.railbook.service;

import com.railbook.railbook.dto.PasswordChangeDto;
import com.railbook.railbook.dto.UserProfileDto;
import com.railbook.railbook.dto.UserRegistrationDto;
import com.railbook.railbook.model.User;
import java.util.List;

public interface UserService {
    User registerUser(UserRegistrationDto registrationDto);
    User findByEmail(String email);
    UserProfileDto getUserProfile(String email);
    void updateProfile(String email, UserProfileDto profileDto);
    void changePassword(String email, PasswordChangeDto passwordDto);
    List<User> getAllUsers();
    long getTotalUserCount();
}
