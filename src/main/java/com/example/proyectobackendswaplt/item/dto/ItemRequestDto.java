package com.example.proyectobackendswaplt.item.dto;

import com.example.proyectobackendswaplt.item.domain.ItemCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull
    private Long categoryId;

    @NotBlank
    @Size(max = 120)
    private String location;

    @NotBlank
    @Size(max = 120)
    private String wantedItem;

    @NotNull
    private ItemCondition condition;
}
