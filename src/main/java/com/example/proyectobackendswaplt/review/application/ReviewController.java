package com.example.proyectobackendswaplt.review.application;

import com.example.proyectobackendswaplt.review.domain.ReviewService;
import com.example.proyectobackendswaplt.review.dto.ReviewMapper;
import com.example.proyectobackendswaplt.review.dto.ReviewRequestDto;
import com.example.proyectobackendswaplt.review.dto.ReviewResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;

    @PostMapping
    public ResponseEntity<ReviewResponseDto> create(@Valid @RequestBody ReviewRequestDto request) {
        ReviewResponseDto response = reviewMapper.toResponseDto(reviewService.create(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDto>> findAll() {
        List<ReviewResponseDto> response = reviewService.findAllVisible().stream().map(reviewMapper::toResponseDto).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReviewResponseDto>> findReceivedByUserId(@PathVariable Long userId) {
        List<ReviewResponseDto> response = reviewService.findReceivedByUserId(userId).stream().map(reviewMapper::toResponseDto).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponseDto> findById(@PathVariable Long id) {
        ReviewResponseDto response = reviewMapper.toResponseDto(reviewService.findById(id));
        return ResponseEntity.ok(response);
    }
}
