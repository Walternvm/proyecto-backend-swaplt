package com.example.proyectobackendswaplt.exchange.domain;

import com.example.proyectobackendswaplt.common.exception.ConflictException;
import com.example.proyectobackendswaplt.common.exception.ForbiddenException;
import com.example.proyectobackendswaplt.common.exception.ResourceNotFoundException;
import com.example.proyectobackendswaplt.exchange.infrastructure.ExchangeRepository;
import com.example.proyectobackendswaplt.proposal.domain.Proposal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final ExchangeRepository exchangeRepository;

    public List<Exchange> findAll() {
        return exchangeRepository.findAll();
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
        checkParticipant(exchange);
        if (exchange.getStatus() != ExchangeStatus.PENDING) {
            throw new ConflictException("Intercambio no pendiente");
        }
        exchange.setStatus(newStatus);
        return exchangeRepository.save(exchange);
    }

    private void checkParticipant(Exchange exchange) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!exchange.getOfferingUser().getEmail().equals(email)
                && !exchange.getReceivingUser().getEmail().equals(email)) {
            throw new ForbiddenException();
        }
    }
}