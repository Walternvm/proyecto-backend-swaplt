package com.example.proyectobackendswaplt.exchange.dto;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;

public record ExchangeResponse(Long id, Long proposalId, Long offeringUserId,
                               Long receivingUserId, String status) {
    public static ExchangeResponse from(Exchange exchange) {
        return new ExchangeResponse(exchange.getId(), exchange.getProposal().getId(),
                exchange.getOfferingUser().getId(), exchange.getReceivingUser().getId(),
                exchange.getStatus().name());
    }
}
