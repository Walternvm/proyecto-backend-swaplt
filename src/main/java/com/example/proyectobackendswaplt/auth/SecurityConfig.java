package com.example.proyectobackendswaplt.auth;

import com.example.proyectobackendswaplt.common.config.ApiRoutes;
import com.example.proyectobackendswaplt.user.domain.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    @Bean
    UserDetailsService userDetailsService(UserService userService) {
        return email -> userService.findByEmail(email)
                .map(account ->
                        User.withUsername(
                                        account.getEmail()
                                )
                                .password(
                                        account.getPassword()
                                )
                                .roles(
                                        account.getRole().name()
                                )
                                .build()
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException(email)
                );
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins}") String[] allowedOrigins) {
        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(allowedOrigins)
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type"
                )
        );

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(
                        AbstractHttpConfigurer::disable
                )
                .formLogin(
                        AbstractHttpConfigurer::disable
                )
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(
                                (request, response, exception) ->
                                        writeError(
                                                request,
                                                response,
                                                HttpStatus.UNAUTHORIZED,
                                                "Debe iniciar sesion para acceder a este recurso"
                                        )
                        )
                        .accessDeniedHandler(
                                (request, response, exception) ->
                                        writeError(
                                                request,
                                                response,
                                                HttpStatus.FORBIDDEN,
                                                "No tiene permisos para realizar esta accion"
                                        )
                        )
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/error")
                        .permitAll()

                        .requestMatchers(
                                ApiRoutes.V1 + "/auth/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                ApiRoutes.V1 + "/items/**",
                                ApiRoutes.V1 + "/categories/**",
                                ApiRoutes.V1 + "/reviews/users/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                ApiRoutes.V1 + "/categories/**"
                        )
                        .hasRole("ADMIN")

                        .requestMatchers(
                                ApiRoutes.V1 + "/users/me",
                                ApiRoutes.V1 + "/users/me/**"
                        )
                        .authenticated()

                        .requestMatchers(
                                ApiRoutes.V1 + "/users/**"
                        )
                        .hasRole("ADMIN")

                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    private static void writeError(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message) throws IOException {

        response.setStatus(status.value());
        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );
        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        String body = String.format(
                "{\"timestamp\":\"%s\","
                        + "\"status\":%d,"
                        + "\"error\":\"%s\","
                        + "\"message\":\"%s\","
                        + "\"path\":\"%s\"}",
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        response.getWriter().write(body);
    }
}
