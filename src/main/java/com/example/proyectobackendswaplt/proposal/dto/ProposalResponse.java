package com.example.proyectobackendswaplt.proposal.dto;

import com.example.proyectobackendswaplt.proposal.domain.Proposal;
import java.time.LocalDateTime;

public record ProposalResponse(Long id, Long proposerId, Long offeredItemId, Long requestedItemId,
                               String status, String message, LocalDateTime createdAt) {
    public static ProposalResponse from(Proposal proposal) {
        return new ProposalResponse(proposal.getId(), proposal.getUser().getId(),
                proposal.getOfferedItem().getId(), proposal.getRequestedItem().getId(),
                proposal.getStatus().name(), proposal.getMessage(), proposal.getCreatedAt());
    }
}
