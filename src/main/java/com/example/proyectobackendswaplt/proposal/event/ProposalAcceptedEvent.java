package com.example.proyectobackendswaplt.proposal.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProposalAcceptedEvent extends ApplicationEvent {
    private final String offeringUserEmail;
    private final String receivingUserEmail;
    private final Long exchangeId;

    public ProposalAcceptedEvent(Object source, String offeringUserEmail, String receivingUserEmail, Long exchangeId) {
        super(source);
        this.offeringUserEmail = offeringUserEmail;
        this.receivingUserEmail = receivingUserEmail;
        this.exchangeId = exchangeId;
    }
}