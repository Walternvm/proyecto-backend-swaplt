package com.example.proyectobackendswaplt.review.domain;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import com.example.proyectobackendswaplt.exchange.domain.ExchangeStatus;
import com.example.proyectobackendswaplt.review.infrastructure.ReviewRepository;
import com.example.proyectobackendswaplt.exchange.infrastructure.ExchangeRepository;
import com.example.proyectobackendswaplt.review.dto.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ExchangeRepository exchangeRepository;

    @Transactional
    public Review create(ReviewRequest request, String email) {
        Exchange exchange = exchangeRepository.findById(request.exchangeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (exchange.getStatus() != ExchangeStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exchange is not completed");
        }

        Review review = new Review();
        review.setRating(request.rating());
        review.setComment(request.comment());
        if (exchange.getOfferingUser().getEmail().equals(email)) {
            review.setAuthor(exchange.getOfferingUser());
            review.setReceiver(exchange.getReceivingUser());
        } else if (exchange.getReceivingUser().getEmail().equals(email)) {
            review.setAuthor(exchange.getReceivingUser());
            review.setReceiver(exchange.getOfferingUser());
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        review.setExchange(exchange);

        return reviewRepository.save(review);
    }

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
    }
}
