package com.example.resumeBuilder.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Value("${jwt.secret-key}")
    private String jwtSecret;

    @Value("${jwt.expiry-time}")
    private long jwtExpiryTime;

    public String generateJwtToken(String userId){
        Map<String, Object> claims = new HashMap<>();
        return createToken(userId,claims);
    }

    private SecretKey getJwtSecretKey(){
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private String createToken(String userId, Map<String, Object> claims){
        return Jwts.builder()
                .subject(userId)
                .claims(claims)
                .header().empty().add("typ","JWT")
                .and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+jwtExpiryTime))
                .signWith(getJwtSecretKey())
                .compact();
    }

    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getJwtSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String jwtToken) {
        Claims claims = extractAllClaims(jwtToken);
        return claims.getExpiration().before(new Date());
    }
}
