package com.example.proyectobackendswaplt.auth.dto;

import com.example.proyectobackendswaplt.user.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private String refreshToken;
    private Long userId;
    private Role role;
}
