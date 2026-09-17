package com.example.proyectobackendswaplt.user.infrastructure;

import com.example.proyectobackendswaplt.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
