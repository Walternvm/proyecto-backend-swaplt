package com.example.proyectobackendswaplt.item.infrastructure;

import com.example.proyectobackendswaplt.category.domain.Category;
import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    List<Item> findByStateAndCategoryInAndUserNotOrderByCreatedAtDesc(ItemState state, Collection<Category> categories, User user);
}