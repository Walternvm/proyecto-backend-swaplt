package com.example.proyectobackendswaplt.exchange.infrastructure;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import com.example.proyectobackendswaplt.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    List<Exchange> findByOfferingUserOrReceivingUser(User offeringUser, User receivingUser);
}