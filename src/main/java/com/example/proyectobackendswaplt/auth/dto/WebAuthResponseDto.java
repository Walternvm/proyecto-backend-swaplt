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
public class WebAuthResponseDto {
    private String token;
    private Long userId;
    private Role role;
}
