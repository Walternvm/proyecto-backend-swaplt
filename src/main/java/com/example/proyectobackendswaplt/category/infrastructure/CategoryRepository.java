package com.example.proyectobackendswaplt.category.infrastructure;

import com.example.proyectobackendswaplt.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
