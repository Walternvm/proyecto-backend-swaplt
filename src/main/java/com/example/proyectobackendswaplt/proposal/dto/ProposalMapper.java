package com.example.proyectobackendswaplt.proposal.dto;

import com.example.proyectobackendswaplt.proposal.domain.Proposal;
import org.springframework.stereotype.Component;

@Component
public class ProposalMapper {

    public Proposal toEntity(ProposalRequestDto request) {
        Proposal proposal = new Proposal();
        proposal.setMessage(request.getMessage());
        return proposal;
    }

    public ProposalResponseDto toResponseDto(Proposal proposal) {
        return ProposalResponseDto.builder()
                .id(proposal.getId())
                .proposerId(proposal.getUser().getId())
                .offeredItemId(proposal.getOfferedItem().getId())
                .requestedItemId(proposal.getRequestedItem().getId())
                .status(proposal.getStatus())
                .message(proposal.getMessage())
                .createdAt(proposal.getCreatedAt())
                .build();
    }
}
