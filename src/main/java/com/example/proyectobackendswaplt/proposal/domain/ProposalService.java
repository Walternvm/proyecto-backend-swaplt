package com.example.proyectobackendswaplt.proposal.domain;

import com.example.proyectobackendswaplt.auth.CurrentUserService;
import com.example.proyectobackendswaplt.common.exception.ConflictException;
import com.example.proyectobackendswaplt.common.exception.ForbiddenException;
import com.example.proyectobackendswaplt.common.exception.ResourceNotFoundException;
import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import com.example.proyectobackendswaplt.exchange.domain.ExchangeService;
import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.domain.ItemService;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.proposal.dto.ProposalMapper;
import com.example.proyectobackendswaplt.proposal.dto.ProposalRequestDto;
import com.example.proyectobackendswaplt.proposal.event.ProposalAcceptedEvent;
import com.example.proyectobackendswaplt.proposal.infrastructure.ProposalRepository;
import com.example.proyectobackendswaplt.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProposalService {
    private final ProposalRepository proposalRepository;
    private final ItemService itemService;
    private final ExchangeService exchangeService;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final ProposalMapper proposalMapper;

    @Transactional
    public Proposal create(ProposalRequestDto request) {
        User currentUser = currentUserService.get();

        Item offeredItem = itemService.findById(request.getOfferedItemId());

        Item requestedItem = itemService.findById(request.getRequestedItemId());

        if (!offeredItem.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Solo puedes ofrecer tus propios items");
        }

        validateCanPropose(offeredItem, requestedItem, currentUser);

        Proposal proposal = proposalMapper.toEntity(request);
        proposal.setUser(currentUser);
        proposal.setOfferedItem(offeredItem);
        proposal.setRequestedItem(requestedItem);
        proposal.setStatus(ProposalStatus.PENDING);

        return proposalRepository.save(proposal);
    }

    @Transactional(readOnly = true)
    public List<Proposal> findAllVisible() {
        if (currentUserService.isAdmin()) {
            return proposalRepository.findAll();
        }

        User currentUser = currentUserService.get();

        return proposalRepository.findByUserOrRequestedItemUserOrderByCreatedAtDesc(currentUser, currentUser);
    }

    @Transactional(readOnly = true)
    public Proposal findVisibleById(Long id) {
        Proposal proposal = findById(id);

        if (!currentUserService.isAdmin() && !isParticipant(proposal, currentUserService.email())) {
            throw new ForbiddenException();
        }

        return proposal;
    }

    @Transactional(readOnly = true)
    public Proposal findById(Long id) {
        return proposalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Propuesta", id));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Exchange acceptProposal(Long proposalId) {
        Proposal proposal = findById(proposalId);

        requireRequestedItemOwner(proposal);
        requirePending(proposal);

        Item offeredItem = proposal.getOfferedItem();
        Item requestedItem = proposal.getRequestedItem();

        if (offeredItem.getState() != ItemState.AVAILABLE || requestedItem.getState() != ItemState.AVAILABLE) {
            throw new ConflictException("Uno o ambos items ya no estan disponibles");
        }

        itemService.markReserved(offeredItem);
        itemService.markReserved(requestedItem);

        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposalRepository.save(proposal);

        Exchange exchange = exchangeService.createFromProposal(proposal);

        invalidateOtherPendingProposals(proposal, offeredItem, requestedItem);

        eventPublisher.publishEvent(
                new ProposalAcceptedEvent(
                        this,
                        proposal.getUser()
                                .getEmail(),
                        proposal.getRequestedItem()
                                .getUser()
                                .getEmail(),
                        exchange.getId()
                )
        );

        return exchange;
    }

    @Transactional
    public Proposal rejectProposal(Long proposalId) {
        Proposal proposal = findById(proposalId);

        requireRequestedItemOwner(proposal);
        requirePending(proposal);

        proposal.setStatus(ProposalStatus.REJECTED);

        return proposalRepository.save(proposal);
    }

    @Transactional
    public Proposal cancelProposal(Long proposalId) {
        Proposal proposal = findById(proposalId);

        if (!proposal.getUser().getId().equals(currentUserService.get().getId())) {
            throw new ForbiddenException("Solo quien envio la propuesta puede retirarla");
        }

        requirePending(proposal);
        proposal.setStatus(ProposalStatus.CANCELLED);

        return proposalRepository.save(proposal);
    }

    private void validateCanPropose(Item offeredItem, Item requestedItem, User currentUser) {
        if (offeredItem.getId().equals(requestedItem.getId())) {
            throw new ConflictException("No puedes intercambiar un item por si mismo");
        }

        if (requestedItem.getUser().getId().equals(currentUser.getId())) {
            throw new ConflictException("No puedes proponer un intercambio contigo mismo");
        }

        if (offeredItem.getState() != ItemState.AVAILABLE) {
            throw new ConflictException("Tu item no esta disponible");
        }

        if (requestedItem.getState() != ItemState.AVAILABLE) {
            throw new ConflictException("El item solicitado no esta disponible");
        }

        boolean duplicateProposal = proposalRepository.existsByOfferedItemAndRequestedItemAndStatus(offeredItem, requestedItem, ProposalStatus.PENDING);

        if (duplicateProposal) {
            throw new ConflictException("Ya enviaste una propuesta pendiente con este item");
        }
    }

    private void requireRequestedItemOwner(Proposal proposal) {
        Long ownerId = proposal.getRequestedItem().getUser().getId();

        Long currentUserId = currentUserService.get().getId();

        if (!ownerId.equals(currentUserId)) {
            throw new ForbiddenException("Solo el propietario del item solicitado puede responder la propuesta");
        }
    }

    private void requirePending(Proposal proposal) {
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new ConflictException("La propuesta ya no esta pendiente");
        }
    }

    private void invalidateOtherPendingProposals(Proposal acceptedProposal, Item offeredItem, Item requestedItem) {
        Set<Proposal> proposalsToInvalidate = new HashSet<>();

        proposalsToInvalidate.addAll(proposalRepository.findByStatusAndOfferedItem(ProposalStatus.PENDING, offeredItem));

        proposalsToInvalidate.addAll(proposalRepository.findByStatusAndRequestedItem(ProposalStatus.PENDING, offeredItem));

        proposalsToInvalidate.addAll(proposalRepository.findByStatusAndOfferedItem(ProposalStatus.PENDING, requestedItem));

        proposalsToInvalidate.addAll(proposalRepository.findByStatusAndRequestedItem(ProposalStatus.PENDING, requestedItem));

        proposalsToInvalidate.remove(acceptedProposal);

        for (Proposal proposal : proposalsToInvalidate) {
            proposal.setStatus(ProposalStatus.INVALIDATED);
        }

        proposalRepository.saveAll(proposalsToInvalidate);
    }

    private boolean isParticipant(Proposal proposal, String email) {
        return proposal.getUser().getEmail().equals(email)
                || proposal.getRequestedItem()
                .getUser()
                .getEmail()
                .equals(email);
    }
}
