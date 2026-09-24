package com.example.proyectobackendswaplt.proposal.application;

import com.example.proyectobackendswaplt.exchange.dto.ExchangeResponse;
import com.example.proyectobackendswaplt.proposal.domain.ProposalService;
import com.example.proyectobackendswaplt.proposal.dto.ProposalRequest;
import com.example.proyectobackendswaplt.proposal.dto.ProposalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proposals")
@RequiredArgsConstructor
public class ProposalController {
    private final ProposalService proposalService;

    @PostMapping
    public ResponseEntity<ProposalResponse> create(@Valid @RequestBody ProposalRequest request,
                                                   Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProposalResponse.from(proposalService.create(request, authentication.getName())));
    }

    @GetMapping
    public ResponseEntity<List<ProposalResponse>> findAll() {
        return ResponseEntity.ok(proposalService.findAllVisible().stream().map(ProposalResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProposalResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ProposalResponse.from(proposalService.findVisibleById(id)));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ExchangeResponse> accept(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ExchangeResponse.from(proposalService.acceptProposal(id)));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ProposalResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ProposalResponse.from(proposalService.rejectProposal(id)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ProposalResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ProposalResponse.from(proposalService.cancelProposal(id)));
    }
}