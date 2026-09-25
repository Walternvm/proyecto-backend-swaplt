package com.example.proyectobackendswaplt.item.infrastructure;

import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.domain.ItemCondition;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.item.dto.ItemFilter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ItemSpecifications {
    private ItemSpecifications() {
    }

    public static Specification<Item> matching(ItemFilter filter) {
        List<Specification<Item>> specifications = new ArrayList<>();

        if (filter.getCategoryId() != null) {
            specifications.add(hasCategory(filter.getCategoryId()));
        }

        if (filter.getState() != null) {
            specifications.add(hasState(filter.getState()));
        }

        if (filter.getCondition() != null) {
            specifications.add(hasCondition(filter.getCondition()));
        }

        if (StringUtils.hasText(filter.getLocation())) {
            specifications.add(locationContains(filter.getLocation()));
        }

        if (StringUtils.hasText(filter.getQ())) {
            specifications.add(textContains(filter.getQ()));
        }

        return Specification.allOf(specifications);
    }

    private static Specification<Item> hasCategory(Long categoryId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("category").get("id"),
                        categoryId
                );
    }

    private static Specification<Item> hasState(ItemState state) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("state"), state);
    }

    private static Specification<Item> hasCondition(ItemCondition condition) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("condition"), condition);
    }

    private static Specification<Item> locationContains(String location) {
        String pattern = "%" + location.trim().toLowerCase() + "%";

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        pattern
                );
    }

    private static Specification<Item> textContains(String text) {
        String pattern = "%" + text.trim().toLowerCase() + "%";

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                pattern
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("description")),
                                pattern
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("wantedItem")),
                                pattern
                        )
                );
    }
}