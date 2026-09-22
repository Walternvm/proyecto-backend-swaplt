package com.example.proyectobackendswaplt.proposal.domain;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import com.example.proyectobackendswaplt.exchange.domain.ExchangeStatus;
import com.example.proyectobackendswaplt.exchange.infrastructure.ExchangeRepository;
import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.item.infrastructure.ItemRepository;
import com.example.proyectobackendswaplt.proposal.infrastructure.ProposalRepository;
import com.example.proyectobackendswaplt.proposal.dto.ProposalRequest;
import com.example.proyectobackendswaplt.publication.domain.Publication;
import com.example.proyectobackendswaplt.publication.domain.PublicationStatus;
import com.example.proyectobackendswaplt.publication.infrastructure.PublicationRepository;
import com.example.proyectobackendswaplt.user.infrastructure.UserRepository;
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
    private final PublicationRepository publicationRepository;
    private final UserRepository userRepository;

    public Proposal create(ProposalRequest request, String email) {
        Item offered = itemRepository.findById(request.offeredItemId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offered item not found"));
        Publication publication = publicationRepository.findById(request.publicationId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publication not found"));
        Item requested = publication.getItem();
        if (!offered.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (offered.getState() != ItemState.AVAILABLE || requested.getState() != ItemState.AVAILABLE
                || offered.getId().equals(requested.getId())
                || publication.getStatus() != PublicationStatus.ACTIVE
                || requested.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Items and publication must be available");
        }
        Proposal proposal = new Proposal();
        proposal.setUser(userRepository.findByEmail(email).orElseThrow());
        proposal.setOfferedItem(offered);
        proposal.setRequestedItem(requested);
        proposal.setPublication(publication);
        proposal.setMessage(request.message());
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
        exchange.setReceivingUser(requestedItem.getUser());
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
