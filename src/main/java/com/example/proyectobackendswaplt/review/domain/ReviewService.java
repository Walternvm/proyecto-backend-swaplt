package com.example.proyectobackendswaplt.review.domain;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import com.example.proyectobackendswaplt.exchange.domain.ExchangeStatus;
import com.example.proyectobackendswaplt.review.infrastructure.ReviewRepository;
import com.example.proyectobackendswaplt.exchange.infrastructure.ExchangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ExchangeRepository exchangeRepository;

    @Transactional
    public Review create(Review review) {
        if (review.getExchange() == null || review.getExchange().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Exchange exchange = exchangeRepository.findById(review.getExchange().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (exchange.getStatus() != ExchangeStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exchange is not completed");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
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
