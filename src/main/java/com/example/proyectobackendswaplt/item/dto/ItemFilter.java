package com.example.proyectobackendswaplt.item.dto;

import com.example.proyectobackendswaplt.item.domain.ItemCondition;
import com.example.proyectobackendswaplt.item.domain.ItemState;

public record ItemFilter(Long categoryId, ItemState state, ItemCondition condition,
                         String location, String q) {
}