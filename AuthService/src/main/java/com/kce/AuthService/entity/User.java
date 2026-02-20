package com.kce.AuthService.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
@Document(collection = "users")
public class User {

    @Id
    private String id; // MongoDB _id

    @Indexed(unique = true)
    private Long userId; // like AUTO_INCREMENT

    private String fullName;

   

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String phoneNo;

    private String password;

    private Role role;


    public User() {
    }

    public User(Long userId, String fullName, String email, String password, Role role, String phoneNo) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phoneNo = phoneNo;
    }

    public String getId() {
        return id;
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

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
}