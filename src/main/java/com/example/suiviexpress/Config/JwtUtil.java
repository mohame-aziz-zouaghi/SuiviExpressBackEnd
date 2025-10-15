package com.example.suiviexpress.Config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private Key key;
    private final long jwtExpirationMs = 86400000; // 1 day default

    @PostConstruct
    public void init() {
        // generate a random HS256 key at startup
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        System.out.println("Generated JWT Key (Base64): " + Encoders.BASE64.encode(key.getEncoded()));
    }

    public String generateToken(Long userId, String username, Object role, boolean rememberMe) {
        Date now = new Date();
        long expiration = rememberMe ? 7*24*60*60*1000L : jwtExpirationMs; // 7 days vs 1 day
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return ((Number) Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("userId")).longValue();
    }

    public Object getRolesFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("role");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
