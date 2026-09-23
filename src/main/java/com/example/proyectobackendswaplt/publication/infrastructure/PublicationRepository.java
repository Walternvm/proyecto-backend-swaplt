package com.example.proyectobackendswaplt.publication.infrastructure;

import com.example.proyectobackendswaplt.publication.domain.Publication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicationRepository extends JpaRepository<Publication, Long> {
}
