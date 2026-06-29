package com.medicare.service;

import com.medicare.dto.UserRegistrationDto;
import com.medicare.model.User;

import java.util.List;

public interface UserService {
    User registerPatient(UserRegistrationDto dto);
    User findByEmail(String email);
    User updateProfile(String email, String fullName, String phone, String gender, String address);
    void changePassword(String email, String currentPassword, String newPassword);
    List<User> getAllPatients();
    void deletePatient(Long id);
}
