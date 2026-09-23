package com.example.proyectobackendswaplt.review.infrastructure;

import com.example.proyectobackendswaplt.review.domain.Review;
import com.example.proyectobackendswaplt.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByReceiverId(Long receiverId);
    List<Review> findByAuthorOrReceiver(User author, User receiver);
}