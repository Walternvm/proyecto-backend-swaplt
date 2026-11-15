package com.example.proyectobackendswaplt.item.dto;

import com.example.proyectobackendswaplt.item.domain.ItemCondition;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

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
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String wantedItem;
    private ItemCondition condition;
    private String imageUrl;
    private LocalDateTime createdAt;
}
