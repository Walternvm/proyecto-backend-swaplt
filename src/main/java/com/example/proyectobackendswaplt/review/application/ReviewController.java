package com.example.proyectobackendswaplt.review.application;

import com.example.proyectobackendswaplt.review.domain.ReviewService;
import com.example.proyectobackendswaplt.review.dto.ReviewRequest;
import com.example.proyectobackendswaplt.review.dto.ReviewResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request,
                                                 Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReviewResponse.from(reviewService.create(request, authentication.getName())));
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> findAll(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(reviewService.findAll(userId).stream().map(ReviewResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ReviewResponse.from(reviewService.findById(id)));
    }
}