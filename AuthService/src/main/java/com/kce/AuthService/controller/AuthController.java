package com.kce.AuthService.controller;

import com.kce.AuthService.dto.AuthRequestDTO;
import com.kce.AuthService.dto.AuthResponseDTO;
import com.kce.AuthService.dto.UserDTO;
import com.kce.AuthService.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ---------------- AUTH ----------------

    // Public login (CUSTOMER/EMPLOYEE + virtual ADMIN)
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO req) {
        return ResponseEntity.ok(userService.login(req));
    }

    // Any authenticated user
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> me(Authentication authentication) {
        return ResponseEntity.ok(userService.me(authentication.getName()));
    }

    // Public register => CUSTOMER only
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody AuthRequestDTO req) {
        return ResponseEntity.ok(userService.registerCustomer(req));
    }

    // ---------------- ADMIN INTERNAL LOOKUPS ----------------

    // Only ADMIN: get role by userId
    @GetMapping("/internal/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> roleByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of("role", userService.getRoleByUserId(userId)));
    }

    // Only ADMIN: get userId by email
 // Only ADMIN: get full user details by email (without password)
    @GetMapping("/internal/by-email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> userByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    // ---------------- ADMIN CREATE ----------------

    /**
     * ADMIN only: create EMPLOYEE or CUSTOMER.
     * - If email ends with @shineride.in / @shineride.com -> creates EMPLOYEE
     * - Else -> creates CUSTOMER (admin-created)
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createByAdmin(@RequestBody AuthRequestDTO req) {
        return ResponseEntity.ok(userService.createUserByAdmin(req));
    }

    // ---------------- ADMIN LIST / DELETE ----------------

    // ADMIN only: list all users (GET /user)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    // ADMIN only: delete user
 // ADMIN only: delete user by email
    @DeleteMapping("/by-email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteByEmail(@PathVariable String email) {
        userService.deleteUserByEmail(email);
        return ResponseEntity.ok("Deleted user with email = " + email);
    }
    // ---------------- EMPLOYEE / ADMIN UPDATE ----------------

    // EMPLOYEE or ADMIN: update user
    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    public ResponseEntity<UserDTO> update(@PathVariable Long userId, @RequestBody AuthRequestDTO req) {
        return ResponseEntity.ok(userService.updateEmployee(userId, req));
    }

    // ---------------- HEALTH ----------------

    @GetMapping("/ping")
    public String ping() {
        return "PING-AUTH-" + System.currentTimeMillis();
    }
    @PatchMapping("/me/username")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<UserDTO> updateMyUsername(Authentication auth,
                                                    @RequestBody Map<String, String> body) {
        String newUsername = body.get("username");
        return ResponseEntity.ok(userService.updateCustomerUsername(auth.getName(), newUsername));
    }

    /** CUSTOMER: update their own phone number */
    @PatchMapping("/me/phone")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<UserDTO> updateMyPhone(Authentication auth,
                                                 @RequestBody Map<String, String> body) {
        String newPhone = body.get("phone");
        return ResponseEntity.ok(userService.updateCustomerPhone(auth.getName(), newPhone));
    }

    /** CUSTOMER: update their own email */
    @PatchMapping("/me/email")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<UserDTO> updateMyEmail(Authentication auth,
                                                 @RequestBody Map<String, String> body) {
        String newEmail = body.get("email");
        return ResponseEntity.ok(userService.updateCustomerEmail(auth.getName(), newEmail));
    }

    /** CUSTOMER: update their own password */
    @PatchMapping("/me/password")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<String> updateMyPassword(Authentication auth,
                                                   @RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        userService.updateCustomerPassword(auth.getName(), oldPassword, newPassword);
        return ResponseEntity.ok("Password updated successfully");
    }
}