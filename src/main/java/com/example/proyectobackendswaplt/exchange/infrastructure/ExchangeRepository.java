package com.example.proyectobackendswaplt.exchange.infrastructure;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
}
