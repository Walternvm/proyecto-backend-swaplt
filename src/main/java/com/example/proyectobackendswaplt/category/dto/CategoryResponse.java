package com.example.proyectobackendswaplt.category.dto;

import com.example.proyectobackendswaplt.category.domain.Category;

public record CategoryResponse(Long id, String name) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
