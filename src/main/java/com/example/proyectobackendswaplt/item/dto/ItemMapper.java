package com.example.proyectobackendswaplt.item.dto;

import com.example.proyectobackendswaplt.item.domain.Item;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public Item toEntity(ItemRequestDto request) {
        Item item = new Item();

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setLocation(request.getLocation());
        item.setWantedItem(request.getWantedItem());
        item.setCondition(request.getCondition());

        return item;
    }

    public ItemResponseDto toResponseDto(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .ownerId(item.getUser().getId())
                .categoryId(item.getCategory().getId())
                .name(item.getName())
                .description(item.getDescription())
                .state(item.getState())
                .location(item.getLocation())
                .wantedItem(item.getWantedItem())
                .condition(item.getCondition())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
