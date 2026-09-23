package com.example.proyectobackendswaplt.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ItemRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description,
        @NotNull Long categoryId,
        @NotBlank @Size(max = 120) String location,
        @NotBlank @Size(max = 120) String wantedItem) {
}
