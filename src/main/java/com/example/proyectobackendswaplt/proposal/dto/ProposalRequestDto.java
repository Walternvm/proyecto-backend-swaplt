package com.example.proyectobackendswaplt.proposal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalRequestDto {
    @NotNull
    private Long offeredItemId;

    @NotNull
    private Long requestedItemId;

    @Size(max = 1000)
    private String message;
}
