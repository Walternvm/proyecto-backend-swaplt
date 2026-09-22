package com.example.proyectobackendswaplt.auth;

import com.example.proyectobackendswaplt.user.domain.Role;
import com.example.proyectobackendswaplt.user.domain.User;
import com.example.proyectobackendswaplt.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:}")
    private String email;

    @Value("${ADMIN_PASSWORD:}")
    private String password;

    @Override
    public void run(String... args) {
        if (!email.isBlank() && !password.isBlank() && users.findByEmail(email).isEmpty()) {
            User admin = new User();
            admin.setName("Administrator");
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRole(Role.ADMIN);
            users.save(admin);
        }
    }
}
