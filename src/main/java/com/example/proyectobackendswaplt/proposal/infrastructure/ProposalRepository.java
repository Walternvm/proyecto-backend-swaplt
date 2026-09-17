package com.example.proyectobackendswaplt.proposal.infrastructure;

import com.example.proyectobackendswaplt.proposal.domain.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {
}
