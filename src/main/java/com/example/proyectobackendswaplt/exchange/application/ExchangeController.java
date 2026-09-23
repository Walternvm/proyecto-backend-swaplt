package com.example.proyectobackendswaplt.exchange.application;

import com.example.proyectobackendswaplt.exchange.domain.ExchangeService;
import com.example.proyectobackendswaplt.exchange.dto.ExchangeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
@RequiredArgsConstructor
public class ExchangeController {
    private final ExchangeService exchangeService;

    @GetMapping
    public ResponseEntity<List<ExchangeResponse>> findAll() {
        return ResponseEntity.ok(exchangeService.findAllVisible().stream().map(ExchangeResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExchangeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ExchangeResponse.from(exchangeService.findVisibleById(id)));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ExchangeResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ExchangeResponse.from(exchangeService.completeExchange(id)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ExchangeResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ExchangeResponse.from(exchangeService.cancelExchange(id)));
    }
}