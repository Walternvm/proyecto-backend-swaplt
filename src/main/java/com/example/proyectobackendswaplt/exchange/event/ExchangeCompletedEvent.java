package com.example.proyectobackendswaplt.exchange.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ExchangeCompletedEvent extends ApplicationEvent {
    private final String offeringUserEmail;
    private final String receivingUserEmail;
    private final Long exchangeId;

    public ExchangeCompletedEvent(Object source, String offeringUserEmail, String receivingUserEmail, Long exchangeId) {
        super(source);
        this.offeringUserEmail = offeringUserEmail;
        this.receivingUserEmail = receivingUserEmail;
        this.exchangeId = exchangeId;
    }
}