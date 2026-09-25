package com.example.proyectobackendswaplt.proposal.dto;

import com.example.proyectobackendswaplt.proposal.domain.ProposalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResponseDto {
    private Long id;
    private Long proposerId;
    private Long offeredItemId;
    private Long requestedItemId;
    private ProposalStatus status;
    private String message;
    private LocalDateTime createdAt;
}
