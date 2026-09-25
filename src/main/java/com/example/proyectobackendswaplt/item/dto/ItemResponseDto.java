package com.example.proyectobackendswaplt.item.dto;

import com.example.proyectobackendswaplt.item.domain.ItemCondition;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponseDto {
    private Long id;
    private Long ownerId;
    private Long categoryId;
    private String name;
    private String description;
    private ItemState state;
    private String location;
    private String wantedItem;
    private ItemCondition condition;
    private LocalDateTime createdAt;
}
