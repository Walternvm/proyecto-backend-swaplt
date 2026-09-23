package com.example.proyectobackendswaplt.user.application;

import com.example.proyectobackendswaplt.auth.CurrentUserService;
import com.example.proyectobackendswaplt.category.dto.CategoryResponse;
import com.example.proyectobackendswaplt.publication.dto.PublicationResponse;
import com.example.proyectobackendswaplt.user.domain.UserPreferenceService;
import com.example.proyectobackendswaplt.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MeController {
    private final CurrentUserService currentUserService;
    private final UserPreferenceService userPreferenceService;

    @GetMapping
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(UserResponse.from(currentUserService.get()));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<PublicationResponse>> getFavorites() {
        return ResponseEntity.ok(userPreferenceService.getFavorites().stream()
                .map(PublicationResponse::from).toList());
    }

    @PutMapping("/favorites/{publicationId}")
    public ResponseEntity<Void> addFavorite(@PathVariable Long publicationId) {
        userPreferenceService.addFavorite(publicationId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/favorites/{publicationId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long publicationId) {
        userPreferenceService.removeFavorite(publicationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/interests")
    public ResponseEntity<List<CategoryResponse>> getInterests() {
        return ResponseEntity.ok(userPreferenceService.getInterests().stream()
                .map(CategoryResponse::from).toList());
    }

    @PutMapping("/interests/{categoryId}")
    public ResponseEntity<Void> addInterest(@PathVariable Long categoryId) {
        userPreferenceService.addInterest(categoryId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/interests/{categoryId}")
    public ResponseEntity<Void> removeInterest(@PathVariable Long categoryId) {
        userPreferenceService.removeInterest(categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<PublicationResponse>> getRecommendations() {
        return ResponseEntity.ok(userPreferenceService.getRecommendations().stream()
                .map(PublicationResponse::from).toList());
    }
}