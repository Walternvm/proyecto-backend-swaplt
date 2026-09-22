package com.example.proyectobackendswaplt.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.example.proyectobackendswaplt.user.domain.User;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expiration;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String createToken(User user) {
        Date now = new Date();
        return Jwts.builder().subject(user.getEmail())
                .claim("userId", user.getId()).claim("role", user.getRole().name()).issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key).compact();
    }

    public String getEmail(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }
}
