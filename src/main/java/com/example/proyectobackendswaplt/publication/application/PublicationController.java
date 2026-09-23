package com.example.proyectobackendswaplt.publication.application;

import com.example.proyectobackendswaplt.publication.domain.PublicationService;
import com.example.proyectobackendswaplt.publication.dto.PublicationRequest;
import com.example.proyectobackendswaplt.publication.dto.PublicationResponse;
import jakarta.validation.Valid;
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
    private final PublicationService publicationService;

    @PostMapping
    public ResponseEntity<PublicationResponse> create(@Valid @RequestBody PublicationRequest request,
                                                      Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PublicationResponse.from(publicationService.create(request, authentication.getName())));
    }

    @GetMapping
    public List<PublicationResponse> findAll() {
        return publicationService.findAll().stream().map(PublicationResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicationResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(PublicationResponse.from(publicationService.findById(id)));
    }
}
