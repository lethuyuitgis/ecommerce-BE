package com.shopcuathuy.security;

import com.shopcuathuy.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    public enum TokenType {
        ACCESS,
        REFRESH
    }

    private final SecretKey signingKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiration}") long accessTokenValidityMs,
        @Value("${app.jwt.refresh-expiration}") long refreshTokenValidityMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    public String generateAccessToken(User user) {
        return generateToken(user, TokenType.ACCESS, accessTokenValidityMs);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, TokenType.REFRESH, refreshTokenValidityMs);
    }

    private String generateToken(User user, TokenType tokenType, long validity) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validity);

        return Jwts.builder()
            .setSubject(user.getId())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .claim("email", user.getEmail())
            .claim("role", user.getUserType() != null ? user.getUserType().name() : User.UserType.CUSTOMER.name())
            .claim("type", tokenType.name())
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public String getUserEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String getUserRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public TokenType getTokenType(String token) {
        String type = parseClaims(token).get("type", String.class);
        return type != null ? TokenType.valueOf(type) : TokenType.ACCESS;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}

