package com.example.proyectobackendswaplt.exchange.infrastructure;

import com.example.proyectobackendswaplt.exchange.domain.Exchange;
import com.example.proyectobackendswaplt.user.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    List<Exchange> findByOfferingUserOrReceivingUser(User offeringUser, User receivingUser);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT exchange FROM Exchange exchange WHERE exchange.id = :id")
    Optional<Exchange> findByIdForUpdate(@Param("id") Long id);
}