package com.example.proyectobackendswaplt.auth;

import com.example.proyectobackendswaplt.auth.dto.AuthResponseDto;
import com.example.proyectobackendswaplt.auth.dto.LoginRequestDto;
import com.example.proyectobackendswaplt.auth.dto.RefreshTokenRequestDto;
import com.example.proyectobackendswaplt.auth.dto.RegisterRequestDto;
import com.example.proyectobackendswaplt.auth.dto.WebAuthResponseDto;
import com.example.proyectobackendswaplt.common.exception.InvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/web")
@RequiredArgsConstructor
public class WebAuthController {
    private final AuthService authService;
    private final WebAuthCookieService cookieService;

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<WebAuthResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request,
            HttpServletResponse response
    ) {
        AuthResponseDto authentication =
                authService.register(request);

        cookieService.writeRefreshToken(
                response,
                authentication.getRefreshToken()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toWebResponse(authentication));
    }

    @PostMapping("/login")
    public ResponseEntity<WebAuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response
    ) {
        AuthResponseDto authentication =
                authService.login(request);

        cookieService.writeRefreshToken(
                response,
                authentication.getRefreshToken()
        );

        return ResponseEntity.ok(
                toWebResponse(authentication)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<WebAuthResponseDto> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            String refreshToken =
                    cookieService.requireRefreshToken(request);

            AuthResponseDto authentication =
                    authService.refresh(
                            new RefreshTokenRequestDto(refreshToken)
                    );

            cookieService.writeRefreshToken(
                    response,
                    authentication.getRefreshToken()
            );

            return ResponseEntity.ok(
                    toWebResponse(authentication)
            );
        } catch (InvalidTokenException exception) {
            cookieService.clearRefreshToken(response);
            throw exception;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        cookieService.findRefreshToken(request)
                .ifPresent(refreshToken ->
                        authService.logout(
                                new RefreshTokenRequestDto(
                                        refreshToken
                                )
                        )
                );

        cookieService.clearRefreshToken(response);

        return ResponseEntity.noContent().build();
    }

    private WebAuthResponseDto toWebResponse(
            AuthResponseDto authentication
    ) {
        return WebAuthResponseDto.builder()
                .token(authentication.getToken())
                .userId(authentication.getUserId())
                .role(authentication.getRole())
                .build();
    }
}
