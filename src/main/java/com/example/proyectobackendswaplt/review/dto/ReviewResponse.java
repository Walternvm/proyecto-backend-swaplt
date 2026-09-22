package com.example.proyectobackendswaplt.review.dto;

import com.example.proyectobackendswaplt.review.domain.Review;

public record ReviewResponse(Long id, Long exchangeId, Long authorId, Long receiverId,
                             Integer rating, String comment) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(review.getId(), review.getExchange().getId(),
                review.getAuthor().getId(), review.getReceiver().getId(),
                review.getRating(), review.getComment());
    }
}
