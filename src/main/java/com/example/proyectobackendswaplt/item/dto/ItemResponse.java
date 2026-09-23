package com.example.proyectobackendswaplt.item.dto;

import com.example.proyectobackendswaplt.item.domain.Item;

import java.time.LocalDateTime;

public record ItemResponse(Long id, Long ownerId, Long categoryId, String name,
                           String description, String state, String location,
                           String wantedItem, LocalDateTime createdAt) {
    public static ItemResponse from(Item item) {
        return new ItemResponse(item.getId(), item.getUser().getId(), item.getCategory().getId(),
                item.getName(), item.getDescription(), item.getState().name(), item.getLocation(),
                item.getWantedItem(), item.getCreatedAt());
    }
}
