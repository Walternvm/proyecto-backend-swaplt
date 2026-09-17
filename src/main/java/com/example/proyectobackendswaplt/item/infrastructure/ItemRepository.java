package com.example.proyectobackendswaplt.item.infrastructure;

import com.example.proyectobackendswaplt.item.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
