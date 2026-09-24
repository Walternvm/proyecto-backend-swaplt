package com.example.proyectobackendswaplt.proposal.event;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import com.example.proyectobackendswaplt.proposal.domain.Proposal;

public record ProposalAcceptedEvent(Proposal proposal, Exchange exchange) {
}
