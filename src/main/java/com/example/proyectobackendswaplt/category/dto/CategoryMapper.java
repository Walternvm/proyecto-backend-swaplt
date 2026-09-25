package com.example.proyectobackendswaplt.category.dto;

import com.example.proyectobackendswaplt.category.domain.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequestDto request) {
        Category category = new Category();
        category.setName(request.getName().trim());
        return category;
    }

    public CategoryResponseDto toResponseDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
