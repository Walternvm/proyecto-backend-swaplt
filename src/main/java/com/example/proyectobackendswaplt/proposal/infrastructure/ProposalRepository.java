package com.example.proyectobackendswaplt.proposal.infrastructure;

import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.proposal.domain.Proposal;
import com.example.proyectobackendswaplt.proposal.domain.ProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findByStatusAndOfferedItem(ProposalStatus status, Item offeredItem);
    List<Proposal> findByStatusAndRequestedItem(ProposalStatus status, Item requestedItem);
}
