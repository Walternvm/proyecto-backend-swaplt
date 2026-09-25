package com.example.proyectobackendswaplt.exchange.dto;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import org.springframework.stereotype.Component;

@Component
public class ExchangeMapper {

    public ExchangeResponseDto toResponseDto(Exchange exchange) {
        return ExchangeResponseDto.builder()
                .id(exchange.getId())
                .proposalId(exchange.getProposal().getId())
                .offeringUserId(exchange.getOfferingUser().getId())
                .receivingUserId(exchange.getReceivingUser().getId())
                .status(exchange.getStatus())
                .offeringUserConfirmed(exchange.isOfferingUserConfirmed())
                .receivingUserConfirmed(exchange.isReceivingUserConfirmed())
                .completedAt(exchange.getCompletedAt())
                .build();
    }
}
