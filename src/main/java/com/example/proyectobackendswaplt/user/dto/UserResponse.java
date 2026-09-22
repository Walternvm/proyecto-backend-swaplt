package com.example.proyectobackendswaplt.user.dto;

import com.example.proyectobackendswaplt.user.domain.User;

public record UserResponse(Long id, String name, String email, String role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
