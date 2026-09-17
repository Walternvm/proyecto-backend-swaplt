package com.example.proyectobackendswaplt.review.infrastructure;

import com.example.proyectobackendswaplt.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
