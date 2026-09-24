package com.example.proyectobackendswaplt.auth.event;

import com.example.proyectobackendswaplt.user.domain.User;

public record UserRegisteredEvent(User user) {
}