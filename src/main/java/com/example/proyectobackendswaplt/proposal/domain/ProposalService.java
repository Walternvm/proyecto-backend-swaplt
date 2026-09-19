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
        toInvalidate.addAll(
                proposalRepository.findByStatusAndOfferedItemOrRequestedItem(
                        ProposalStatus.PENDING, offeredItem, offeredItem)
        );
        toInvalidate.addAll(
                proposalRepository.findByStatusAndOfferedItemOrRequestedItem(
                        ProposalStatus.PENDING, requestedItem, requestedItem)
        );
        toInvalidate.remove(accepted);

        for (Proposal p : toInvalidate) {
            p.setStatus(ProposalStatus.INVALIDATED);
        }
        proposalRepository.saveAll(toInvalidate);
    }

    @Transactional
    public void rejectProposal(Long proposalId) {
        Proposal proposal = findById(proposalId);
        proposal.setStatus(ProposalStatus.REJECTED);
        proposalRepository.save(proposal);
    }
}
