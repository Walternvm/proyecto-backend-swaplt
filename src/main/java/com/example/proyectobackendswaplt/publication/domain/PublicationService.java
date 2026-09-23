package com.example.proyectobackendswaplt.publication.domain;

import com.example.proyectobackendswaplt.category.domain.Category;
import com.example.proyectobackendswaplt.common.exception.ConflictException;
import com.example.proyectobackendswaplt.common.exception.ForbiddenException;
import com.example.proyectobackendswaplt.common.exception.ResourceNotFoundException;
import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.domain.ItemService;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.publication.dto.PublicationRequest;
import com.example.proyectobackendswaplt.publication.infrastructure.PublicationRepository;
import com.example.proyectobackendswaplt.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicationService {
    private final PublicationRepository publicationRepository;
    private final ItemService itemService;

    @Transactional
    public Publication create(PublicationRequest request, String email) {
        Item item = itemService.findById(request.itemId());
        if (!item.getUser().getEmail().equals(email)) {
            throw new ForbiddenException();
        }
        if (item.getState() != ItemState.AVAILABLE) {
            throw new ConflictException("Item no disponible");
        }
        Publication publication = new Publication();
        publication.setItem(item);
        publication.setUser(item.getUser());
        publication.setWantedItem(request.wantedItem());
        return publicationRepository.save(publication);
    }

    public List<Publication> findAll() {
        return publicationRepository.findAll();
    }

    public Publication findById(Long id) {
        return publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicacion", id));
    }

    public List<Publication> findActiveByCategoriesExcludingUser(Collection<Category> categories, User user) {
        if (categories.isEmpty()) {
            return List.of();
        }
        return publicationRepository.findByStatusAndItemCategoryInAndUserNotOrderByCreatedAtDesc(
                PublicationStatus.ACTIVE, categories, user);
    }
}