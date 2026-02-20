package com.kce.AuthService.service.impl;

import com.kce.AuthService.dto.AuthRequestDTO;
import com.kce.AuthService.dto.AuthResponseDTO;
import com.kce.AuthService.dto.UserDTO;
import com.kce.AuthService.entity.Role;
import com.kce.AuthService.entity.User;
import com.kce.AuthService.repository.UserRepository;
import com.kce.AuthService.security.JwtService;
import com.kce.AuthService.service.SequenceGeneratorService;
import com.kce.AuthService.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final String USER_SEQ = "users_seq";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            SequenceGeneratorService sequenceGeneratorService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    // ---------------- helpers ----------------

    private String normEmail(String email) {
        return email == null ? "" : email.toLowerCase().trim();
    }

    private String normPhone(String phone) {
        return phone == null ? "" : phone.trim();
    }

    private boolean isEmployeeDomain(String email) {
        return email.endsWith("@shineride.in") || email.endsWith("@shineride.com");
    }

    private boolean isGmail(String email) {
        return email.endsWith("@gmail.com");
    }

    private void requireBasics(AuthRequestDTO req) {
        if (req.getFullName() == null || req.getFullName().isBlank())
            throw new IllegalArgumentException("Full name is required");
        if (req.getEmail() == null || req.getEmail().isBlank())
            throw new IllegalArgumentException("Email is required");
        if (req.getPassword() == null || req.getPassword().isBlank())
            throw new IllegalArgumentException("Password is required");
    }

    private User getByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
    }

    private User getByUserIdOrThrow(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with userId: " + userId));
    }

    private UserDTO toDto(User u) {
        return new UserDTO(
                u.getUserId(),
                u.getFullName(),
                u.getEmail(),
                u.getRole().name(),
                u.getPhoneNo()
        );
    }

    private UserDTO createCustomerInternal(AuthRequestDTO req) {
        String email = normEmail(req.getEmail());
        String phone = normPhone(req.getPhoneNo());

        if (isEmployeeDomain(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Employee email cannot be registered/created as customer");
        }
        if (!isGmail(email)) {
            throw new IllegalArgumentException("Only @gmail.com can be CUSTOMER");
        }
        if (phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByPhoneNo(phone)) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        long nextUserId = sequenceGeneratorService.nextId(USER_SEQ);

        User user = new User();
        user.setUserId(nextUserId);
        user.setFullName(req.getFullName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setPhoneNo(phone);

        User saved = userRepository.save(user);
        return toDto(saved);
    }

    // ---------------- PUBLIC REGISTER ----------------

    // PUBLIC: /user/register  -> CUSTOMER only
    @Override
    public AuthResponseDTO registerCustomer(AuthRequestDTO req) {
        requireBasics(req);

        UserDTO dto = createCustomerInternal(req);

        // login token after register (your current behavior)
        String token = jwtService.generateToken(dto.getEmail(), Role.CUSTOMER.name());
        return new AuthResponseDTO(token, dto.getUserId(), dto.getEmail(), Role.CUSTOMER.name());
    }

    // ---------------- ADMIN CREATE ----------------

    // ADMIN: /user/create -> can create EMPLOYEE or CUSTOMER
    @Override
    public UserDTO createUserByAdmin(AuthRequestDTO req) {
        requireBasics(req);

        String email = normEmail(req.getEmail());

        // Employee creation
        if (isEmployeeDomain(email)) {
            return createEmployee(req);
        }

        // Customer creation (admin-created)
        return createCustomerInternal(req);
    }

    // ---------------- INTERNAL LOOKUPS ----------------
    @Override
    public UserDTO getUserByEmail(String email) {
        String e = email == null ? "" : email.toLowerCase().trim();

        // Optional: allow returning virtual admin details too
        if (e.equalsIgnoreCase(adminEmail.toLowerCase().trim())) {
            return new UserDTO(0L, "ADMIN", adminEmail, "ADMIN", null);
        }

        User user = userRepository.findByEmail(e)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        return toDto(user); // your toDto does NOT include password ✅
    }
    @Override
    public String getRoleByUserId(Long userId) {
        User u = getByUserIdOrThrow(userId);
        return u.getRole().name();
    }

    @Override
    public Long getUserIdByEmail(String email) {
        User u = getByEmailOrThrow(normEmail(email));
        return u.getUserId();
    }

    // ---------------- LOGIN ----------------

    @Override
    public AuthResponseDTO login(AuthRequestDTO req) {
        String email = normEmail(req.getEmail());
        String password = req.getPassword();

        // virtual admin (not stored in DB)
        if (email.equalsIgnoreCase(normEmail(adminEmail)) && password != null && password.equals(adminPassword)) {
            String token = jwtService.generateToken(email, "ADMIN");
            return new AuthResponseDTO(token, 0L, email, "ADMIN");
        }

        User user = getByEmailOrThrow(email);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponseDTO(token, user.getUserId(), user.getEmail(), user.getRole().name());
    }

    // ---------------- PHONE UTILS ----------------

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhoneNo(normPhone(phone));
    }

    @Override
    public User findByPhone(String phone) {
        return userRepository.findByPhoneNo(normPhone(phone))
                .orElseThrow(() -> new RuntimeException("User not found with phone: " + phone));
    }

    // ---------------- ME ----------------

    @Override
    public UserDTO me(String emailFromJwt) {
        String email = normEmail(emailFromJwt);

        // If you want "me" to work for virtual admin too:
        if (email.equalsIgnoreCase(normEmail(adminEmail))) {
            return new UserDTO(0L, "ADMIN", adminEmail, "ADMIN", null);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toDto(user);
    }

    // ---------------- EMPLOYEE CREATE ----------------

    // ADMIN flow calls this internally; controller can also call it directly if needed
    @Override
    public UserDTO createEmployee(AuthRequestDTO req) {
        requireBasics(req);

        String email = normEmail(req.getEmail());
        String phone = normPhone(req.getPhoneNo());

        if (!isEmployeeDomain(email)) {
            throw new IllegalArgumentException("Employee email must end with @shineride.in or @shineride.com");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (!phone.isBlank() && userRepository.existsByPhoneNo(phone)) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        long nextUserId = sequenceGeneratorService.nextId(USER_SEQ);

        User emp = new User();
        emp.setUserId(nextUserId);
        emp.setFullName(req.getFullName());
        emp.setEmail(email);
        emp.setPassword(passwordEncoder.encode(req.getPassword()));
        emp.setRole(Role.EMPLOYEE);
        emp.setPhoneNo(phone.isBlank() ? null : phone);

        return toDto(userRepository.save(emp));
    }

    // ---------------- UPDATE / LIST / DELETE ----------------

    @Override
    public UserDTO updateEmployee(Long userId, AuthRequestDTO req) {
        User u = getByUserIdOrThrow(userId);

        if (req.getFullName() != null && !req.getFullName().isBlank()) u.setFullName(req.getFullName());

        if (req.getPhoneNo() != null && !req.getPhoneNo().isBlank()) {
            String newPhone = normPhone(req.getPhoneNo());
            if (!newPhone.equals(u.getPhoneNo()) && userRepository.existsByPhoneNo(newPhone)) {
                throw new IllegalArgumentException("Phone number already exists");
            }
            u.setPhoneNo(newPhone);
        }

        // optional email update
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            String newEmail = normEmail(req.getEmail());
            if (!newEmail.equals(u.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Email already exists");
            }
            u.setEmail(newEmail);
        }

        // optional password update
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        userRepository.save(u);
        return toDto(u);
    }

    @Override
    public List<UserDTO> listUsers() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public void deleteUserByEmail(String email) {
        String e = email == null ? "" : email.toLowerCase().trim();

        // Prevent deleting virtual admin
        if (e.equalsIgnoreCase(adminEmail.toLowerCase().trim())) {
            throw new IllegalArgumentException("Cannot delete virtual ADMIN");
        }

        User user = userRepository.findByEmail(e)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        userRepository.delete(user);
    }
    @Override
    public UserDTO updateCustomerUsername(String emailFromJwt, String fullName) {
        String email = normEmail(emailFromJwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        if (user.getRole() != Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only CUSTOMER can update their profile");
        }

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }

        user.setFullName(fullName.trim());
        userRepository.save(user);
        return toDto(user);
    }

    @Override
    public UserDTO updateCustomerPhone(String emailFromJwt, String phone) {
        String email = normEmail(emailFromJwt);
        String newPhone = normPhone(phone);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        if (user.getRole() != Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only CUSTOMER can update their profile");
        }

        if (newPhone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        if (!newPhone.equals(user.getPhoneNo()) && userRepository.existsByPhoneNo(newPhone)) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        user.setPhoneNo(newPhone);
        userRepository.save(user);
        return toDto(user);
    }

    @Override
    public UserDTO updateCustomerEmail(String emailFromJwt, String newEmailRaw) {
        String oldEmail = normEmail(emailFromJwt);
        String newEmail = normEmail(newEmailRaw);

        User user = userRepository.findByEmail(oldEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + oldEmail));

        if (user.getRole() != Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only CUSTOMER can update their profile");
        }

        if (newEmail.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        // keep your existing CUSTOMER rule
        if (isEmployeeDomain(newEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Employee email cannot be used as customer email");
        }
        if (!isGmail(newEmail)) {
            throw new IllegalArgumentException("Only @gmail.com can be CUSTOMER");
        }

        if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setEmail(newEmail);
        userRepository.save(user);
        return toDto(user);
    }

    @Override
    public void updateCustomerPassword(String emailFromJwt, String oldPassword, String newPassword) {
        String email = normEmail(emailFromJwt);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        if (user.getRole() != Role.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only CUSTOMER can update their profile");
        }

        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException("Old password is required");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password is required");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}