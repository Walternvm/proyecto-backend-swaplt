package com.example.proyectobackendswaplt.proposal.application;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import com.example.proyectobackendswaplt.proposal.domain.Proposal;
import com.example.proyectobackendswaplt.proposal.domain.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
public class ProposalController {
    private final ProposalService proposalService;

    @PostMapping
    public ResponseEntity<Proposal> create(@RequestBody Proposal proposal) {
        Proposal created = proposalService.create(proposal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Proposal>> findAll() {
        return ResponseEntity.ok(proposalService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proposal> findById(@PathVariable Long id) {
        return ResponseEntity.ok(proposalService.findById(id));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<Exchange> accept(@PathVariable Long id) {
        Exchange exchange = proposalService.acceptProposal(id);
        return ResponseEntity.ok(exchange);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id) {
        proposalService.rejectProposal(id);
        return ResponseEntity.noContent().build();
    }
}
