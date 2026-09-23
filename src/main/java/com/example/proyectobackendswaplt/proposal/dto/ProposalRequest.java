package com.example.proyectobackendswaplt.proposal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProposalRequest(@NotNull Long offeredItemId, @NotNull Long requestedItemId,
                              @Size(max = 1000) String message) {
}
