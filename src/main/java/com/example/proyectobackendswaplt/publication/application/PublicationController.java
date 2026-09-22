package com.example.proyectobackendswaplt.publication.application;

import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.item.infrastructure.ItemRepository;
import com.example.proyectobackendswaplt.publication.domain.Publication;
import com.example.proyectobackendswaplt.publication.infrastructure.PublicationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/publications")
@RequiredArgsConstructor
public class PublicationController {
    private final PublicationRepository publicationRepository;
    private final ItemRepository itemRepository;

    public record CreatePublication(@NotNull Long itemId, @NotBlank @Size(max = 120) String wantedItem) {}

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreatePublication request, Authentication authentication) {
        Item item = itemRepository.findById(request.itemId()).orElse(null);
        if (item == null) return ResponseEntity.notFound().build();
        if (!item.getUser().getEmail().equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (item.getState() != ItemState.AVAILABLE) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Item is not available");
        }
        Publication publication = new Publication();
        publication.setItem(item);
        publication.setUser(item.getUser());
        publication.setWantedItem(request.wantedItem());
        return ResponseEntity.status(HttpStatus.CREATED).body(publicationRepository.save(publication));
    }

    @GetMapping
    public List<Publication> findAll() {
        return publicationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Publication> findById(@PathVariable Long id) {
        return publicationRepository.findById(id).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
