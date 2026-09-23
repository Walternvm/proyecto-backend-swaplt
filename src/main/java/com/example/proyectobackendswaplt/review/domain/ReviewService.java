package com.example.proyectobackendswaplt.review.domain;

import com.example.proyectobackendswaplt.auth.CurrentUserService;
import com.example.proyectobackendswaplt.common.exception.ConflictException;
import com.example.proyectobackendswaplt.common.exception.ForbiddenException;
import com.example.proyectobackendswaplt.common.exception.ResourceNotFoundException;
import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import com.example.proyectobackendswaplt.exchange.domain.ExchangeService;
import com.example.proyectobackendswaplt.exchange.domain.ExchangeStatus;
import com.example.proyectobackendswaplt.review.dto.ReviewRequest;
import com.example.proyectobackendswaplt.review.infrastructure.ReviewRepository;
import com.example.proyectobackendswaplt.user.domain.User;
import com.example.proyectobackendswaplt.user.domain.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ExchangeService exchangeService;
    private final UserService userService;
    private final CurrentUserService currentUserService;

    @Transactional
    public Review create(ReviewRequest request, String email) {
        Exchange exchange = exchangeService.findById(request.exchangeId());
        Review review = new Review();
        assignParticipants(review, exchange, email);

        if (exchange.getStatus() != ExchangeStatus.COMPLETED) {
            throw new ConflictException("Solo puedes calificar intercambios completados");
        }
        if (reviewRepository.existsByExchangeIdAndAuthorId(exchange.getId(), review.getAuthor().getId())) {
            throw new ConflictException("Ya calificaste este intercambio");
        }

        review.setExchange(exchange);
        review.setRating(request.rating());
        review.setComment(request.comment());
        return reviewRepository.save(review);
    }

    public List<Review> findAll(Long userId) {
        if (userId != null) {
            userService.findById(userId);
            return reviewRepository.findByReceiverId(userId);
        }
        if (currentUserService.isAdmin()) {
            return reviewRepository.findAll();
        }
        User current = currentUserService.get();
        return reviewRepository.findByAuthorOrReceiver(current, current);
    }

    public Review findById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", id));
    }

    private void assignParticipants(Review review, Exchange exchange, String email) {
        if (exchange.getOfferingUser().getEmail().equals(email)) {
            review.setAuthor(exchange.getOfferingUser());
            review.setReceiver(exchange.getReceivingUser());
        } else if (exchange.getReceivingUser().getEmail().equals(email)) {
            review.setAuthor(exchange.getReceivingUser());
            review.setReceiver(exchange.getOfferingUser());
        } else {
            throw new ForbiddenException("Solo los participantes del intercambio pueden calificarlo");
        }
    }
}