package com.example.proyectobackendswaplt.auth.dto;

public record AuthResponse(String token, String refreshToken, Long userId, String role) {
}