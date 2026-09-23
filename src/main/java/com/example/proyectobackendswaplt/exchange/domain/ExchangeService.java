package com.example.proyectobackendswaplt.exchange.domain;

import com.example.proyectobackendswaplt.auth.CurrentUserService;
import com.example.proyectobackendswaplt.common.exception.ConflictException;
import com.example.proyectobackendswaplt.common.exception.ForbiddenException;
import com.example.proyectobackendswaplt.common.exception.ResourceNotFoundException;
import com.example.proyectobackendswaplt.exchange.infrastructure.ExchangeRepository;
import com.example.proyectobackendswaplt.proposal.domain.Proposal;
import com.example.proyectobackendswaplt.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final ExchangeRepository exchangeRepository;
    private final CurrentUserService currentUserService;

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
    public Exchange completeExchange(Long id) {
        return changeStatus(id, ExchangeStatus.COMPLETED);
    }

    @Transactional
    public Exchange cancelExchange(Long id) {
        return changeStatus(id, ExchangeStatus.CANCELLED);
    }

    private Exchange changeStatus(Long id, ExchangeStatus newStatus) {
        Exchange exchange = findById(id);
        if (!isParticipant(exchange, currentUserService.email())) {
            throw new ForbiddenException();
        }
        if (exchange.getStatus() != ExchangeStatus.PENDING) {
            throw new ConflictException("Intercambio no pendiente");
        }
        exchange.setStatus(newStatus);
        return exchangeRepository.save(exchange);
    }

    private boolean isParticipant(Exchange exchange, String email) {
        return exchange.getOfferingUser().getEmail().equals(email)
                || exchange.getReceivingUser().getEmail().equals(email);
    }
}