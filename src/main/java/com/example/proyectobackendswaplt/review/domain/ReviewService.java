package com.example.proyectobackendswaplt.review.domain;

import com.example.proyectobackendswaplt.auth.CurrentUserService;
import com.example.proyectobackendswaplt.common.exception.ConflictException;
import com.example.proyectobackendswaplt.common.exception.ForbiddenException;
import com.example.proyectobackendswaplt.common.exception.ResourceNotFoundException;
import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import com.example.proyectobackendswaplt.exchange.domain.ExchangeService;
import com.example.proyectobackendswaplt.exchange.domain.ExchangeStatus;
import com.example.proyectobackendswaplt.review.dto.ReviewMapper;
import com.example.proyectobackendswaplt.review.dto.ReviewRequestDto;
import com.example.proyectobackendswaplt.review.infrastructure.ReviewRepository;
import com.example.proyectobackendswaplt.user.domain.User;
import com.example.proyectobackendswaplt.user.domain.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final ReviewMapper reviewMapper;

    @Transactional
    public Review create(ReviewRequestDto request) {
        Exchange exchange = exchangeService.findById(
                request.getExchangeId()
        );

        User currentUser = currentUserService.get();
        Review review = reviewMapper.toEntity(request);

        assignParticipants(review, exchange, currentUser);

        if (exchange.getStatus() != ExchangeStatus.COMPLETED) {
            throw new ConflictException("Solo puedes calificar intercambios completados");
        }

        boolean alreadyReviewed = reviewRepository.existsByExchangeIdAndAuthorId(exchange.getId(), currentUser.getId());

        if (alreadyReviewed) {
            throw new ConflictException("Ya calificaste este intercambio");
        }

        review.setExchange(exchange);

        try {
            return reviewRepository.saveAndFlush(review);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Ya calificaste este intercambio");
        }
    }

    @Transactional(readOnly = true)
    public List<Review> findAllVisible() {
        if (currentUserService.isAdmin()) {
            return reviewRepository.findAll();
        }

        User currentUser = currentUserService.get();

        return reviewRepository.findByAuthorOrReceiver(currentUser, currentUser);
    }

    @Transactional(readOnly = true)
    public List<Review> findReceivedByUserId(Long userId) {
        userService.findById(userId);

        return reviewRepository.findByReceiverId(userId);
    }

    @Transactional(readOnly = true)
    public Review findById(Long id) {
        return reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review", id));
    }

    private void assignParticipants(Review review, Exchange exchange, User currentUser) {
        if (exchange.getOfferingUser().getId().equals(currentUser.getId())) {
            review.setAuthor(exchange.getOfferingUser());
            review.setReceiver(exchange.getReceivingUser());
        }
        else if (exchange.getReceivingUser().getId().equals(currentUser.getId())) {
            review.setAuthor(exchange.getReceivingUser());
            review.setReceiver(exchange.getOfferingUser());
        }
        else {
            throw new ForbiddenException("Solo los participantes del intercambio pueden calificarlo");
        }
    }
}
