package com.example.proyectobackendswaplt.auth;

import com.example.proyectobackendswaplt.auth.dto.AuthResponse;
import com.example.proyectobackendswaplt.auth.dto.LoginRequest;
import com.example.proyectobackendswaplt.auth.dto.RefreshTokenRequest;
import com.example.proyectobackendswaplt.auth.dto.RegisterRequest;
import com.example.proyectobackendswaplt.common.exception.ConflictException;
import com.example.proyectobackendswaplt.common.exception.InvalidCredentialsException;
import com.example.proyectobackendswaplt.user.domain.Role;
import com.example.proyectobackendswaplt.user.domain.User;
import com.example.proyectobackendswaplt.user.domain.UserService;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new ConflictException("Email ya registrado");
        }
        User user = createUser(request.name(), request.email(), request.password(), Role.USER);
        return toResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userService.findByEmail(request.email())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
                .orElseThrow(InvalidCredentialsException::new);
        return toResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        User user = refreshTokenService.consume(request.refreshToken());
        return toResponse(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    @Transactional
    public void createAdminIfMissing(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()
                || userService.existsByEmail(email)) {
            return;
        }
        createUser("Administrador", email, password, Role.ADMIN);
    }

    private User createUser(String name, String email, String rawPassword, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userService.save(user);
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(jwtService.createToken(user), refreshTokenService.create(user),
                user.getId(), user.getRole().name());
    }
}