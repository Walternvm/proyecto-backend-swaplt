package com.example.proyectobackendswaplt.auth;

import com.example.proyectobackendswaplt.common.exception.InvalidTokenException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Service
public class WebAuthCookieService {
    private final String cookieName;
    private final boolean secure;
    private final String sameSite;
    private final String path;
    private final Duration maxAge;

    public WebAuthCookieService(
            @Value("${app.auth.refresh-cookie.name}") String cookieName,
            @Value("${app.auth.refresh-cookie.secure}") boolean secure,
            @Value("${app.auth.refresh-cookie.same-site}") String sameSite,
            @Value("${app.auth.refresh-cookie.path}") String path,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.cookieName = cookieName;
        this.secure = secure;
        this.sameSite = sameSite;
        this.path = path;
        this.maxAge = Duration.ofMillis(refreshExpiration);
    }

    public void writeRefreshToken(
            HttpServletResponse response,
            String refreshToken
    ) {
        ResponseCookie cookie = ResponseCookie
                .from(cookieName, refreshToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(maxAge)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }

    public String requireRefreshToken(HttpServletRequest request) {
        return findRefreshToken(request)
                .orElseThrow(InvalidTokenException::new);
    }

    public Optional<String> findRefreshToken(
            HttpServletRequest request
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie ->
                        cookieName.equals(cookie.getName())
                )
                .map(Cookie::getValue)
                .filter(value ->
                        value != null && !value.isBlank()
                )
                .findFirst();
    }

    public void clearRefreshToken(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }
}
