package com.kce.AuthService.service;

import com.kce.AuthService.dto.AuthRequestDTO;
import com.kce.AuthService.dto.AuthResponseDTO;
import com.kce.AuthService.dto.UserDTO;
import com.kce.AuthService.entity.User;

import java.util.List;

public interface UserService {

    // PUBLIC: /user/register  -> CUSTOMER only
    AuthResponseDTO registerCustomer(AuthRequestDTO req);

    // ADMIN: /user/create  -> creates EMPLOYEE or CUSTOMER
    UserDTO createUserByAdmin(AuthRequestDTO req);

    // Auth
    AuthResponseDTO login(AuthRequestDTO req);
    UserDTO me(String email);

    // Employee (you can keep this for older controller usage if needed)
    UserDTO createEmployee(AuthRequestDTO req);

    // Internal lookups
    String getRoleByUserId(Long userId);
    Long getUserIdByEmail(String email);

    // Admin ops
    UserDTO updateEmployee(Long userId, AuthRequestDTO req);
    List<UserDTO> listUsers();void deleteUserByEmail(String email);
    UserDTO getUserByEmail(String email);
    // Phone utils
    boolean existsByPhone(String phone);
    User findByPhone(String phone);
    
    
    UserDTO updateCustomerUsername(String email, String username);

    UserDTO updateCustomerPhone(String email, String phone);

    UserDTO updateCustomerEmail(String oldEmail, String newEmail);

    void updateCustomerPassword(String email, String oldPassword, String newPassword);
}