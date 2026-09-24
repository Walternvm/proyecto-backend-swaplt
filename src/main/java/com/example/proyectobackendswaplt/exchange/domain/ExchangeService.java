package com.example.proyectobackendswaplt.exchange.domain;

import com.example.proyectobackendswaplt.auth.CurrentUserService;
import com.example.proyectobackendswaplt.common.exception.ConflictException;
import com.example.proyectobackendswaplt.common.exception.ForbiddenException;
import com.example.proyectobackendswaplt.common.exception.ResourceNotFoundException;
import com.example.proyectobackendswaplt.exchange.infrastructure.ExchangeRepository;
import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.domain.ItemService;
import com.example.proyectobackendswaplt.proposal.domain.Proposal;
import com.example.proyectobackendswaplt.publication.domain.PublicationService;
import com.example.proyectobackendswaplt.user.domain.User;
import com.example.proyectobackendswaplt.exchange.event.ExchangeCompletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final ExchangeRepository exchangeRepository;
    private final CurrentUserService currentUserService;
    private final ItemService itemService;
    private final PublicationService publicationService;
    private final ApplicationEventPublisher eventPublisher;

    public List<Exchange> findAllVisible() {
        if (currentUserService.isAdmin()) {
            return exchangeRepository.findAll();
        }
        User current = currentUserService.get();
        return exchangeRepository.findByOfferingUserOrReceivingUser(current, current);
    }

    public Exchange findVisibleById(Long id) {
        Exchange exchange = findById(id);
        if (!currentUserService.isAdmin() && !isParticipant(exchange, currentUserService.email())) {
            throw new ForbiddenException();
        }
        return exchange;
    }

    public Exchange findById(Long id) {
        return exchangeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Intercambio", id));
    }

    public Exchange createFromProposal(Proposal proposal) {
        Exchange exchange = new Exchange();
        exchange.setProposal(proposal);
        exchange.setOfferingUser(proposal.getUser());
        exchange.setReceivingUser(proposal.getRequestedItem().getUser());
        exchange.setStatus(ExchangeStatus.PENDING);
        return exchangeRepository.save(exchange);
    }

    @Transactional
    public Exchange confirmExchange(Long id) {
        Exchange exchange = findPendingForParticipant(id);
        if (exchange.getOfferingUser().getEmail().equals(currentUserService.email())) {
            if (exchange.isOfferingUserConfirmed()) {
                throw new ConflictException("Ya confirmaste este intercambio");
            }
            exchange.setOfferingUserConfirmed(true);
        } else {
            if (exchange.isReceivingUserConfirmed()) {
                throw new ConflictException("Ya confirmaste este intercambio");
            }
            exchange.setReceivingUserConfirmed(true);
        }
        if (exchange.isOfferingUserConfirmed() && exchange.isReceivingUserConfirmed()) {
            complete(exchange);
        }
        return exchangeRepository.save(exchange);
    }

    @Transactional
    public Exchange cancelExchange(Long id) {
        Exchange exchange = findPendingForParticipant(id);
        exchange.setStatus(ExchangeStatus.CANCELLED);
        for (Item item : itemsOf(exchange)) {
            itemService.markAvailable(item);
        }
        return exchangeRepository.save(exchange);
    }

    private void complete(Exchange exchange) {
        exchange.setStatus(ExchangeStatus.COMPLETED);
        exchange.setCompletedAt(LocalDateTime.now());
        for (Item item : itemsOf(exchange)) {
            itemService.markTraded(item);
            publicationService.closeActiveByItem(item);
        }
        eventPublisher.publishEvent(new ExchangeCompletedEvent(exchange));
    }

    private Exchange findPendingForParticipant(Long id) {
        Exchange exchange = findById(id);
        if (!isParticipant(exchange, currentUserService.email())) {
            throw new ForbiddenException("Solo los participantes pueden modificar este intercambio");
        }
        if (exchange.getStatus() != ExchangeStatus.PENDING) {
            throw new ConflictException("El intercambio ya no esta pendiente");
        }
        return exchange;
    }

    private List<Item> itemsOf(Exchange exchange) {
        Proposal proposal = exchange.getProposal();
        return List.of(proposal.getOfferedItem(), proposal.getRequestedItem());
    }

    private boolean isParticipant(Exchange exchange, String email) {
        return exchange.getOfferingUser().getEmail().equals(email)
                || exchange.getReceivingUser().getEmail().equals(email);
    }
}