package com.example.proyectobackendswaplt.publication.dto;

import com.example.proyectobackendswaplt.publication.domain.Publication;
import java.time.LocalDateTime;

public record PublicationResponse(Long id, Long ownerId, Long itemId, LocalDateTime createdAt,
                                  String status, String wantedItem) {
    public static PublicationResponse from(Publication publication) {
        return new PublicationResponse(publication.getId(), publication.getUser().getId(),
                publication.getItem().getId(), publication.getCreatedAt(),
                publication.getStatus().name(), publication.getWantedItem());
    }
}
