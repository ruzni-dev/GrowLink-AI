package com.growlink.backend.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-seconds:900}") long expirationSeconds) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be set before starting the backend");
        }
        this.signingKey = createSigningKey(secret);
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(String username) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirationSeconds * 1000);
        return Jwts.builder()
                .claims(Map.of("sub", username))
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getPayload().getSubject();
    }

    public boolean isValid(String token, String username) {
        try {
            Claims claims = parse(token).getPayload();
            return username.equals(claims.getSubject()) && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
    }

    private SecretKey createSigningKey(String secret) {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (IllegalArgumentException exception) {
            byte[] rawSecret = secret.getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(rawSecret);
        }
    }
}
