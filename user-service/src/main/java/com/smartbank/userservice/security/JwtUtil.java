package com.smartbank.userservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    private final SecretKey signingKey;
    private final JwtParser jwtParser;
    private final long accessTokenExpiration;

    public JwtUtil(@Value("$jwt.secret") String secret,
                   @Value("$jwt.expiration") long accessTokenExpiration,
                   @Value("${jwt.refresh-expiration}") long refreshTokenExpiration) {

        byte[] keyBytes = Base64.getDecoder().decode(secret);

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = accessTokenExpiration;

        this.jwtParser = Jwts.parser()
                .verifyWith(signingKey)
                .build();
    }

// TOKEN GENERATION

public String generateAccessToken(String email, String role){
        return Jwts.builder()
                .claims(Map.of("role", role
                        , "type", "ACCESS"
                ))
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(signingKey)
                .compact();
}


// TOKEN VALIDATION

public boolean isTokenValid(String token, String email){
        return extractEmail(token).equals(email) && !isTokenExpired(token);
}

// TOKEN EXTRACTION

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }



}
