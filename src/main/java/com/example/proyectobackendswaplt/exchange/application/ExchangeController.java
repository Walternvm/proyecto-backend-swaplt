package com.example.proyectobackendswaplt.exchange.application;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import com.example.proyectobackendswaplt.exchange.domain.ExchangeService;
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
    public ResponseEntity<List<Exchange>> findAll() {
        return ResponseEntity.ok(exchangeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exchange> findById(@PathVariable Long id) {
        return ResponseEntity.ok(exchangeService.findById(id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Exchange> complete(@PathVariable Long id) {
        return ResponseEntity.ok(exchangeService.completeExchange(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Exchange> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(exchangeService.cancelExchange(id));
    }
}
