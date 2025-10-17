package com.varun.wayfinder.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private SecretKey secretKey;
    private static final long EXPIRATION_TIME = 864_000_000; // 10 days

    public JwtUtil(@Value("${jwt.secret:}") String secret) {
        // If no secret is provided, generate a new one
        if (secret == null || secret.trim().isEmpty()) {
            this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
            // Ensure the secret is properly padded for Base64
            try {
                byte[] keyBytes = Base64.getDecoder().decode(secret);
                this.secretKey = Keys.hmacShaKeyFor(keyBytes);
            } catch (IllegalArgumentException e) {
                // If the provided secret is not valid Base64, use it as-is (but this is not recommended for production)
                this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean isTokenExpired(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration()
                    .before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername != null && tokenUsername.equals(username) && !isTokenExpired(token));
    }

}