package com.kce.AuthService.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.expirationMs = expirationMs;

        System.out.println("CouponService jwt.secret length=" + jwtSecret.length());
        System.out.println("CouponService jwt.secret fingerprint=" + fingerprint(jwtSecret));
    }

    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            Date exp = claims.getExpiration();
            return exp != null && exp.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("JWT INVALID in CouponService: " + e.getClass().getName() + " -> " + e.getMessage());
            return false;
        }
    }
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)     // key must be a FIELD in JwtService
                .compact();
    }
    public String extractEmail(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            Object role = parseClaims(token).get("role");
            return role == null ? null : role.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String fingerprint(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.substring(0, 16);
        } catch (Exception e) {
            return "ERR";
        }
    }
}