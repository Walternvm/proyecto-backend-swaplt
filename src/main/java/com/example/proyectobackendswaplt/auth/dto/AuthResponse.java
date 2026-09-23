package com.example.proyectobackendswaplt.auth.dto;

public record AuthResponse(String token, Long userId, String role) {
}