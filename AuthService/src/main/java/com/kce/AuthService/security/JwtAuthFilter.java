package com.kce.AuthService.security;

import com.kce.AuthService.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (!jwtService.isValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token); // CUSTOMER / EMPLOYEE / ADMIN

        if (email == null || role == null) {
            filterChain.doFilter(request, response);
            return;
        }

        email = email.toLowerCase();

        // ✅ ADMIN is "virtual" -> skip DB existence check
        if (!("ADMIN".equals(role) && email.equals(adminEmail.toLowerCase()))) {
            if (userRepository.findByEmail(email).isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        var authority = new SimpleGrantedAuthority("ROLE_" + role);
        var authToken = new UsernamePasswordAuthenticationToken(
                email, null, List.of(authority)
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // ✅ IMPORTANT: continue to controller
        filterChain.doFilter(request, response);
    }
}