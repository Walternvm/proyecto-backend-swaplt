package com.example.proyectobackendswaplt.review.dto;

import com.example.proyectobackendswaplt.review.domain.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public Review toEntity(ReviewRequestDto request) {
        Review review = new Review();
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return review;
    }

    public ReviewResponseDto toResponseDto(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .exchangeId(review.getExchange().getId())
                .authorId(review.getAuthor().getId())
                .receiverId(review.getReceiver().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .build();
    }
}
