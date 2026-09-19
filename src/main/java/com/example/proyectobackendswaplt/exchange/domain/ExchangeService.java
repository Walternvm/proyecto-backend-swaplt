package com.example.proyectobackendswaplt.exchange.domain;

import com.example.proyectobackendswaplt.exchange.infrastructure.ExchangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final ExchangeRepository exchangeRepository;

    public List<Exchange> findAll(){
        return exchangeRepository.findAll();
    }

    public Exchange findById(Long id) {
        return exchangeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exchange not found with id: " + id));
    }

    @Transactional
    public Exchange completeExchange(Long id) {
        Exchange exchange = findById(id);
        exchange.setStatus(ExchangeStatus.COMPLETED);
        return exchangeRepository.save(exchange);
    }

    @Transactional
    public Exchange cancelExchange(Long id) {
        Exchange exchange = findById(id);
        exchange.setStatus(ExchangeStatus.CANCELLED);
        return exchangeRepository.save(exchange);
    }
}
