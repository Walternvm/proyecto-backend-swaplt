package com.example.proyectobackendswaplt.user.domain;

import com.example.proyectobackendswaplt.auth.CurrentUserService;
import com.example.proyectobackendswaplt.category.domain.Category;
import com.example.proyectobackendswaplt.category.domain.CategoryService;
import com.example.proyectobackendswaplt.common.exception.ConflictException;
import com.example.proyectobackendswaplt.publication.domain.Publication;
import com.example.proyectobackendswaplt.publication.domain.PublicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {
    private final CurrentUserService currentUserService;
    private final UserService userService;
    private final PublicationService publicationService;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public List<Publication> getFavorites() {
        return List.copyOf(currentUserService.get().getFavorites());
    }

    @Transactional
    public void addFavorite(Long publicationId) {
        User user = currentUserService.get();
        Publication publication = publicationService.findById(publicationId);
        if (publication.getUser().getId().equals(user.getId())) {
            throw new ConflictException("No puedes guardar tu propia publicacion como favorita");
        }
        boolean alreadySaved = user.getFavorites().stream()
                .anyMatch(favorite -> favorite.getId().equals(publicationId));
        if (!alreadySaved) {
            user.getFavorites().add(publication);
            userService.save(user);
        }
    }

    @Transactional
    public void removeFavorite(Long publicationId) {
        User user = currentUserService.get();
        if (user.getFavorites().removeIf(favorite -> favorite.getId().equals(publicationId))) {
            userService.save(user);
        }
    }

    @Transactional(readOnly = true)
    public List<Category> getInterests() {
        return List.copyOf(currentUserService.get().getCategoriesOfInterest());
    }

    @Transactional
    public void addInterest(Long categoryId) {
        User user = currentUserService.get();
        Category category = categoryService.findById(categoryId);
        boolean alreadyAdded = user.getCategoriesOfInterest().stream()
                .anyMatch(interest -> interest.getId().equals(categoryId));
        if (!alreadyAdded) {
            user.getCategoriesOfInterest().add(category);
            userService.save(user);
        }
    }

    @Transactional
    public void removeInterest(Long categoryId) {
        User user = currentUserService.get();
        if (user.getCategoriesOfInterest().removeIf(interest -> interest.getId().equals(categoryId))) {
            userService.save(user);
        }
    }

    @Transactional(readOnly = true)
    public List<Publication> getRecommendations() {
        User user = currentUserService.get();
        return publicationService.findActiveByCategoriesExcludingUser(user.getCategoriesOfInterest(), user);
    }
}