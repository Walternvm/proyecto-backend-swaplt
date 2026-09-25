package com.example.proyectobackendswaplt.exchange.application;

import com.example.proyectobackendswaplt.exchange.domain.ExchangeService;
import com.example.proyectobackendswaplt.exchange.dto.ExchangeMapper;
import com.example.proyectobackendswaplt.exchange.dto.ExchangeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exchanges")
@RequiredArgsConstructor
public class ExchangeController {
    private final ExchangeService exchangeService;
    private final ExchangeMapper exchangeMapper;

    @GetMapping
    public ResponseEntity<List<ExchangeResponseDto>> findAll() {
        List<ExchangeResponseDto> response = exchangeService.findAllVisible().stream().map(exchangeMapper::toResponseDto).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExchangeResponseDto> findById(@PathVariable Long id) {
        ExchangeResponseDto response = exchangeMapper.toResponseDto(exchangeService.findVisibleById(id));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ExchangeResponseDto> confirm(@PathVariable Long id) {
        ExchangeResponseDto response = exchangeMapper.toResponseDto(exchangeService.confirmExchange(id));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ExchangeResponseDto> cancel(@PathVariable Long id) {
        ExchangeResponseDto response = exchangeMapper.toResponseDto(exchangeService.cancelExchange(id));
        return ResponseEntity.ok(response);
    }
}
