package com.example.proyectobackendswaplt.item.dto;

import com.example.proyectobackendswaplt.item.domain.ItemCondition;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemFilter {
    private Long categoryId;
    private ItemState state;
    private ItemCondition condition;
    private String location;
    private String q;
}
