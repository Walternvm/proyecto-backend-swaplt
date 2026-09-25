package com.example.proyectobackendswaplt.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private Long id;
    private Long exchangeId;
    private Long authorId;
    private Long receiverId;
    private Integer rating;
    private String comment;
}
