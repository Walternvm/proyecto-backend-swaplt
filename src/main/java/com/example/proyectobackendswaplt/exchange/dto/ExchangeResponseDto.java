package com.example.proyectobackendswaplt.exchange.dto;

import com.example.proyectobackendswaplt.exchange.domain.ExchangeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeResponseDto {
    private Long id;
    private Long proposalId;
    private Long offeringUserId;
    private Long receivingUserId;
    private ExchangeStatus status;
    private boolean offeringUserConfirmed;
    private boolean receivingUserConfirmed;
    private LocalDateTime completedAt;
}
