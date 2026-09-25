package com.example.proyectobackendswaplt.auth;

import com.example.proyectobackendswaplt.auth.dto.AuthResponseDto;
import com.example.proyectobackendswaplt.auth.dto.LoginRequestDto;
import com.example.proyectobackendswaplt.auth.dto.RefreshTokenRequestDto;
import com.example.proyectobackendswaplt.auth.dto.RegisterRequestDto;
import com.example.proyectobackendswaplt.auth.event.UserRegisteredEvent;
import com.example.proyectobackendswaplt.common.exception.ConflictException;
import com.example.proyectobackendswaplt.common.exception.InvalidCredentialsException;
import com.example.proyectobackendswaplt.user.domain.Role;
import com.example.proyectobackendswaplt.user.domain.User;
import com.example.proyectobackendswaplt.user.domain.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email ya registrado");
        }

        User user = createUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                Role.USER
        );

        eventPublisher.publishEvent(new UserRegisteredEvent(this, user.getName(), user.getEmail()));

        return toResponse(user);
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        User user = userService
                .findByEmail(request.getEmail())
                .filter(found ->
                        passwordEncoder.matches(
                                request.getPassword(),
                                found.getPassword()
                        )
                )
                .orElseThrow(
                        InvalidCredentialsException::new
                );

        return toResponse(user);
    }

    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto request) {
        User user = refreshTokenService.consume(request.getRefreshToken());

        return toResponse(user);
    }

    @Transactional
    public void logout(RefreshTokenRequestDto request) {
        refreshTokenService.revoke(request.getRefreshToken());
    }

    @Transactional
    public void createAdminIfMissing(String email, String password) {
        if (email == null
                || email.isBlank()
                || password == null
                || password.isBlank()
                || userService.existsByEmail(email)) {
            return;
        }

        createUser(
                "Administrador",
                email,
                password,
                Role.ADMIN
        );
    }

    private User createUser(String name, String email, String rawPassword, Role role) {
        User user = new User();

        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);

        return userService.save(user);
    }

    private AuthResponseDto toResponse(User user) {
        return AuthResponseDto.builder()
                .token(jwtService.createToken(user))
                .refreshToken(
                        refreshTokenService.create(user)
                )
                .userId(user.getId())
                .role(user.getRole())
                .build();
    }
}
