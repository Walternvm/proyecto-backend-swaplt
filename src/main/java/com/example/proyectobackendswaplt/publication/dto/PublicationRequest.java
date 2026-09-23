package com.example.proyectobackendswaplt.publication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PublicationRequest(@NotNull Long itemId, @NotBlank @Size(max = 120) String wantedItem) {
}
