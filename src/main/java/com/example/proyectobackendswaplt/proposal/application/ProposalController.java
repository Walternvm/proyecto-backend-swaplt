package com.example.proyectobackendswaplt.proposal.application;

import com.example.proyectobackendswaplt.exchange.dto.ExchangeMapper;
import com.example.proyectobackendswaplt.exchange.dto.ExchangeResponseDto;
import com.example.proyectobackendswaplt.proposal.domain.ProposalService;
import com.example.proyectobackendswaplt.proposal.dto.ProposalMapper;
import com.example.proyectobackendswaplt.proposal.dto.ProposalRequestDto;
import com.example.proyectobackendswaplt.proposal.dto.ProposalResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proposals")
@RequiredArgsConstructor
public class ProposalController {
    private final ProposalService proposalService;
    private final ProposalMapper proposalMapper;
    private final ExchangeMapper exchangeMapper;

    @PostMapping
    public ResponseEntity<ProposalResponseDto> create(@Valid @RequestBody ProposalRequestDto request) {
        ProposalResponseDto response = proposalMapper.toResponseDto(proposalService.create(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProposalResponseDto>> findAll() {

        List<ProposalResponseDto> response = proposalService.findAllVisible().stream().map(proposalMapper::toResponseDto).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProposalResponseDto> findById(@PathVariable Long id) {
        ProposalResponseDto response = proposalMapper.toResponseDto(proposalService.findVisibleById(id));

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<ExchangeResponseDto> accept(@PathVariable Long id) {
        ExchangeResponseDto response = exchangeMapper.toResponseDto(proposalService.acceptProposal(id));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ProposalResponseDto> reject(@PathVariable Long id) {
        ProposalResponseDto response = proposalMapper.toResponseDto(proposalService.rejectProposal(id));

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ProposalResponseDto> cancel(@PathVariable Long id) {
        ProposalResponseDto response = proposalMapper.toResponseDto(proposalService.cancelProposal(id));

        return ResponseEntity.ok(response);
    }
}
