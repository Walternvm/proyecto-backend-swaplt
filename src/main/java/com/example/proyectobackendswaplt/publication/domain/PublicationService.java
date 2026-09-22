package com.example.proyectobackendswaplt.publication.domain;

import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.item.infrastructure.ItemRepository;
import com.example.proyectobackendswaplt.publication.dto.PublicationRequest;
import com.example.proyectobackendswaplt.publication.infrastructure.PublicationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PublicationService {
    private final PublicationRepository publicationRepository;
    private final ItemRepository itemRepository;

    public Publication create(PublicationRequest request, String email) {
        Item item = itemRepository.findById(request.itemId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        if (!item.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (item.getState() != ItemState.AVAILABLE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item is not available");
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
        return publicationRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publication not found"));
    }
}
