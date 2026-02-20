package com.kce.AuthService.dto;

public class UserDTO {

    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private String phoneNo;

    public UserDTO(Long userId, String fullName, String email, String role, String phoneNo) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.phoneNo = phoneNo;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getPhoneNo() {
        return phoneNo;
    }
}