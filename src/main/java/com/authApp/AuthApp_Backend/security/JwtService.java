// src/main/java/com/authApp/AuthApp_Backend/security/JwtService.java
package com.authApp.AuthApp_Backend.security;

import com.authApp.AuthApp_Backend.entities.Role;
import com.authApp.AuthApp_Backend.entities.User;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    // Add JWT creation/validation methods here as needed
    public String generateAccessToken(User user){
        Instant now = Instant.now();
        List<String> roles = user.getRoleSet() == null ? List.of() :
                user.getRoleSet().stream().map(Role::getName).toList();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .claims(Map.of(
                        "roles", roles,
                        "email", user.getEmail(),
                        "typ", "access"
                ))
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    public String generateRefreshToken(User user, String jti){
        Instant now = Instant.now();
        return Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .claim("typ", "refresh")
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    public Jws<Claims> parse(String token){
        try{
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        }catch (JwtException e){
            throw e;
        }
    }

    public boolean isAccessToken(String token){
        Claims claims = parse(token).getPayload();
        return "access".equals(claims.get("typ", String.class));
    }

    public boolean isRefreshToken(String token){
        Claims claims = parse(token).getPayload();
        return "refresh".equals(claims.get("typ", String.class));
    }

    public UUID getUserId(String token){
        Claims claims = parse(token).getPayload();
        return UUID.fromString(claims.getSubject());
    }

    public String getJti(String token){
        Claims c = parse(token).getPayload();
        return c.getId();
    }

    public List<String> getRoles(String token){
        Claims c = parse(token).getPayload();
        return (List<String>) c.get("roles");
    }

    public String getEmail(String token){
        Claims c = parse(token).getPayload();
        return (String) c.get("email");
    }

}
