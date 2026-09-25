package com.example.proyectobackendswaplt.auth;

import com.example.proyectobackendswaplt.common.exception.InvalidTokenException;
import com.example.proyectobackendswaplt.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class RefreshTokenService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpiration;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.refreshTokenRepository = refreshTokenRepository;

        this.refreshExpiration = refreshExpiration;
    }

    @Transactional
    public String create(User user) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(generateValue());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshExpiration)));

        return refreshTokenRepository.save(refreshToken).getToken();
    }

    @Transactional
    public User consume(String value) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenForUpdate(value).orElseThrow(InvalidTokenException::new);

        LocalDateTime now = LocalDateTime.now();

        if (refreshToken.isRevoked() || !refreshToken.getExpiresAt().isAfter(now)) {
            throw new InvalidTokenException();
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return refreshToken.getUser();
    }

    @Transactional
    public void revoke(String value) {
        refreshTokenRepository
                .findByTokenForUpdate(value)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(
                            refreshToken
                    );
                });
    }

    private String generateValue() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
