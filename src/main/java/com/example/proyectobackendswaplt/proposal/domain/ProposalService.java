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
import com.example.proyectobackendswaplt.proposal.dto.ProposalRequest;
import com.example.proyectobackendswaplt.proposal.infrastructure.ProposalRepository;
import com.example.proyectobackendswaplt.publication.domain.Publication;
import com.example.proyectobackendswaplt.publication.domain.PublicationService;
import com.example.proyectobackendswaplt.publication.domain.PublicationStatus;
import com.example.proyectobackendswaplt.user.domain.User;
import com.example.proyectobackendswaplt.user.domain.UserService;
import com.example.proyectobackendswaplt.proposal.event.ProposalAcceptedEvent;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ItemService itemService;
    private final PublicationService publicationService;
    private final UserService userService;
    private final ExchangeService exchangeService;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Proposal create(ProposalRequest request, String email) {
        Item offered = itemService.findById(request.offeredItemId());
        Publication publication = publicationService.findById(request.publicationId());
        Item requested = publication.getItem();
        if (!offered.getUser().getEmail().equals(email)) {
            throw new ForbiddenException("Solo puedes ofrecer tus propios items");
        }
        validateCanPropose(offered, requested, publication, email);

        Proposal proposal = new Proposal();
        proposal.setUser(userService.getByEmail(email));
        proposal.setOfferedItem(offered);
        proposal.setRequestedItem(requested);
        proposal.setPublication(publication);
        proposal.setMessage(request.message());
        proposal.setStatus(ProposalStatus.PENDING);
        return proposalRepository.save(proposal);
    }

    public List<Proposal> findAllVisible() {
        if (currentUserService.isAdmin()) {
            return proposalRepository.findAll();
        }
        User current = currentUserService.get();
        return proposalRepository.findByUserOrRequestedItemUserOrderByCreatedAtDesc(current, current);
    }

    public Proposal findVisibleById(Long id) {
        Proposal proposal = findById(id);
        if (!currentUserService.isAdmin() && !isParticipant(proposal, currentUserService.email())) {
            throw new ForbiddenException();
        }
        return proposal;
    }

    public Proposal findById(Long id) {
        return proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propuesta", id));
    }

    @Transactional
    public Exchange acceptProposal(Long proposalId) {
        Proposal proposal = findById(proposalId);
        requirePublicationOwner(proposal);
        requirePending(proposal);

        Item offeredItem = proposal.getOfferedItem();
        Item requestedItem = proposal.getRequestedItem();
        if (offeredItem.getState() != ItemState.AVAILABLE
                || requestedItem.getState() != ItemState.AVAILABLE) {
            throw new ConflictException("Uno o ambos items ya no estan disponibles");
        }

        itemService.markReserved(offeredItem);
        itemService.markReserved(requestedItem);
        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposalRepository.save(proposal);

        Exchange exchange = exchangeService.createFromProposal(proposal);
        eventPublisher.publishEvent(new ProposalAcceptedEvent(proposal, exchange));
        invalidateOtherPendingProposals(proposal, offeredItem, requestedItem);
        return exchange;
    }

    @Transactional
    public Proposal rejectProposal(Long proposalId) {
        Proposal proposal = findById(proposalId);
        requirePublicationOwner(proposal);
        requirePending(proposal);
        proposal.setStatus(ProposalStatus.REJECTED);
        return proposalRepository.save(proposal);
    }

    @Transactional
    public Proposal cancelProposal(Long proposalId) {
        Proposal proposal = findById(proposalId);
        if (!proposal.getUser().getEmail().equals(currentUserService.email())) {
            throw new ForbiddenException("Solo quien envio la propuesta puede retirarla");
        }
        requirePending(proposal);
        proposal.setStatus(ProposalStatus.CANCELLED);
        return proposalRepository.save(proposal);
    }

    private void validateCanPropose(Item offered, Item requested, Publication publication, String email) {
        if (requested.getUser().getEmail().equals(email)) {
            throw new ConflictException("No puedes proponer un intercambio a tu propia publicacion");
        }
        if (publication.getStatus() != PublicationStatus.ACTIVE) {
            throw new ConflictException("La publicacion no esta activa");
        }
        if (offered.getState() != ItemState.AVAILABLE) {
            throw new ConflictException("Tu item no esta disponible");
        }
        if (requested.getState() != ItemState.AVAILABLE) {
            throw new ConflictException("El item publicado no esta disponible");
        }
        if (proposalRepository.existsByOfferedItemIdAndPublicationIdAndStatus(
                offered.getId(), publication.getId(), ProposalStatus.PENDING)) {
            throw new ConflictException("Ya enviaste una propuesta pendiente con este item");
        }
    }

    private void requirePublicationOwner(Proposal proposal) {
        if (!proposal.getRequestedItem().getUser().getEmail().equals(currentUserService.email())) {
            throw new ForbiddenException("Solo el dueno de la publicacion puede responder la propuesta");
        }
    }

    private void requirePending(Proposal proposal) {
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new ConflictException("La propuesta ya no esta pendiente");
        }
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

    private boolean isParticipant(Proposal proposal, String email) {
        return proposal.getUser().getEmail().equals(email)
                || proposal.getRequestedItem().getUser().getEmail().equals(email);
    }
}