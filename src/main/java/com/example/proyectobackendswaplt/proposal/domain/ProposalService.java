package com.example.proyectobackendswaplt.proposal.domain;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import com.example.proyectobackendswaplt.exchange.domain.ExchangeStatus;
import com.example.proyectobackendswaplt.exchange.infrastructure.ExchangeRepository;
import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.item.infrastructure.ItemRepository;
import com.example.proyectobackendswaplt.proposal.infrastructure.ProposalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProposalService {
    private final ProposalRepository proposalRepository;
    private final ItemRepository itemRepository;
    private final ExchangeRepository exchangeRepository;

    public Proposal create(Proposal proposal) {
        if (proposal.getOfferedItem() == null || proposal.getRequestedItem() == null
                || proposal.getOfferedItem().getId() == null || proposal.getRequestedItem().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Two items are required");
        }
        Item offered = itemRepository.findById(proposal.getOfferedItem().getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offered item not found"));
        Item requested = itemRepository.findById(proposal.getRequestedItem().getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested item not found"));
        if (!offered.getUser().getEmail().equals(currentEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (offered.getState() != ItemState.AVAILABLE || requested.getState() != ItemState.AVAILABLE
                || offered.getId().equals(requested.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Items must be different and available");
        }
        proposal.setOfferedItem(offered);
        proposal.setRequestedItem(requested);
        proposal.setStatus(ProposalStatus.PENDING);
        return proposalRepository.save(proposal);
    }

    public List<Proposal> findAll() {
        return proposalRepository.findAll();
    }

    public Proposal findById(Long id) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proposal not found with id: " + id));
    }

    @Transactional
    public Exchange acceptProposal(Long proposalId) {
        Proposal proposal = findById(proposalId);
        if (!proposal.getRequestedItem().getUser().getEmail().equals(currentEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Proposal is not pending");
        }

        Item offeredItem = proposal.getOfferedItem();
        Item requestedItem = proposal.getRequestedItem();

        if (offeredItem.getState() != ItemState.AVAILABLE
                || requestedItem.getState() != ItemState.AVAILABLE) {
            throw new RuntimeException("One or both items are no longer available");
        }

        offeredItem.setState(ItemState.RESERVED);
        requestedItem.setState(ItemState.RESERVED);
        itemRepository.save(offeredItem);
        itemRepository.save(requestedItem);

        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposalRepository.save(proposal);

        Exchange exchange = new Exchange();
        exchange.setProposal(proposal);
        exchange.setOfferingUser(proposal.getUser());
        exchange.setReceivingUser(offeredItem.getUser());
        exchange.setStatus(ExchangeStatus.PENDING);
        exchange = exchangeRepository.save(exchange);

        invalidateOtherPendingProposals(proposal, offeredItem, requestedItem);

        return exchange;
    }

    private void invalidateOtherPendingProposals(Proposal accepted, Item offeredItem, Item requestedItem) {
        Set<Proposal> toInvalidate = new HashSet<>();
        toInvalidate.addAll(proposalRepository.findByStatusAndOfferedItem(ProposalStatus.PENDING, offeredItem));
        toInvalidate.addAll(proposalRepository.findByStatusAndRequestedItem(ProposalStatus.PENDING, offeredItem));
        toInvalidate.addAll(proposalRepository.findByStatusAndOfferedItem(ProposalStatus.PENDING, requestedItem));
        toInvalidate.addAll(proposalRepository.findByStatusAndRequestedItem(ProposalStatus.PENDING, requestedItem));
        toInvalidate.remove(accepted);

        for (Proposal p : toInvalidate) {
            p.setStatus(ProposalStatus.INVALIDATED);
        }
        proposalRepository.saveAll(toInvalidate);
    }

    @Transactional
    public void rejectProposal(Long proposalId) {
        Proposal proposal = findById(proposalId);
        if (!proposal.getRequestedItem().getUser().getEmail().equals(currentEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Proposal is not pending");
        }
        proposal.setStatus(ProposalStatus.REJECTED);
        proposalRepository.save(proposal);
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
