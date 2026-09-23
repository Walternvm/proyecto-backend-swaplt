package com.example.proyectobackendswaplt.exchange.dto;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;

import java.time.LocalDateTime;

public record ExchangeResponse(Long id, Long proposalId, Long offeringUserId, Long receivingUserId,
                               String status, boolean offeringUserConfirmed,
                               boolean receivingUserConfirmed, LocalDateTime completedAt) {
    public static ExchangeResponse from(Exchange exchange) {
        return new ExchangeResponse(exchange.getId(), exchange.getProposal().getId(),
                exchange.getOfferingUser().getId(), exchange.getReceivingUser().getId(),
                exchange.getStatus().name(), exchange.isOfferingUserConfirmed(),
                exchange.isReceivingUserConfirmed(), exchange.getCompletedAt());
    }
}