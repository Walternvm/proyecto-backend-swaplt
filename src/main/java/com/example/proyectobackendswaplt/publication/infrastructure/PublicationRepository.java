package com.example.proyectobackendswaplt.publication.infrastructure;

import com.example.proyectobackendswaplt.category.domain.Category;
import com.example.proyectobackendswaplt.publication.domain.Publication;
import com.example.proyectobackendswaplt.publication.domain.PublicationStatus;
import com.example.proyectobackendswaplt.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PublicationRepository extends JpaRepository<Publication, Long> {
    List<Publication> findByStatusAndItemCategoryInAndUserNotOrderByCreatedAtDesc(
            PublicationStatus status, Collection<Category> categories, User user);
}