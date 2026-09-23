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
        if (filter.categoryId() != null) {
            specifications.add(hasCategory(filter.categoryId()));
        }
        if (filter.state() != null) {
            specifications.add(hasState(filter.state()));
        }
        if (filter.condition() != null) {
            specifications.add(hasCondition(filter.condition()));
        }
        if (StringUtils.hasText(filter.location())) {
            specifications.add(locationContains(filter.location()));
        }
        if (StringUtils.hasText(filter.q())) {
            specifications.add(textContains(filter.q()));
        }
        return Specification.allOf(specifications);
    }

    private static Specification<Item> hasCategory(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    private static Specification<Item> hasState(ItemState state) {
        return (root, query, cb) -> cb.equal(root.get("state"), state);
    }

    private static Specification<Item> hasCondition(ItemCondition condition) {
        return (root, query, cb) -> cb.equal(root.get("condition"), condition);
    }

    private static Specification<Item> locationContains(String location) {
        String pattern = "%" + location.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.<String>get("location")), pattern);
    }

    private static Specification<Item> textContains(String text) {
        String pattern = "%" + text.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.<String>get("name")), pattern),
                cb.like(cb.lower(root.<String>get("description")), pattern));
    }
}