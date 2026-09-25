package com.example.proyectobackendswaplt.user.domain;

import com.example.proyectobackendswaplt.auth.CurrentUserService;
import com.example.proyectobackendswaplt.category.domain.Category;
import com.example.proyectobackendswaplt.category.domain.CategoryService;
import com.example.proyectobackendswaplt.common.exception.ConflictException;
import com.example.proyectobackendswaplt.item.domain.Item;
import com.example.proyectobackendswaplt.item.domain.ItemService;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {
    private final CurrentUserService currentUserService;
    private final UserService userService;
    private final ItemService itemService;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public List<Item> getFavorites() {
        User user = currentUserService.get();

        return List.copyOf(user.getFavorites());
    }

    @Transactional
    public void addFavorite(Long itemId) {
        User user = currentUserService.get();
        Item item = itemService.findById(itemId);

        if (item.getUser().getId().equals(user.getId())) {
            throw new ConflictException("No puedes guardar tu propio item como favorito");
        }

        if (item.getState() != ItemState.AVAILABLE) {
            throw new ConflictException("Solo puedes guardar items disponibles como favoritos");
        }

        boolean alreadySaved = user.getFavorites().stream().anyMatch(favorite -> favorite.getId().equals(itemId));

        if (!alreadySaved) {
            user.getFavorites().add(item);
            userService.save(user);
        }
    }

    @Transactional
    public void removeFavorite(Long itemId) {
        User user = currentUserService.get();

        boolean removed = user.getFavorites().removeIf(favorite -> favorite.getId().equals(itemId));

        if (removed) {
            userService.save(user);
        }
    }

    @Transactional(readOnly = true)
    public List<Category> getInterests() {
        User user = currentUserService.get();

        return List.copyOf(user.getCategoriesOfInterest());
    }

    @Transactional
    public void addInterest(Long categoryId) {
        User user = currentUserService.get();
        Category category = categoryService.findById(categoryId);

        boolean alreadyAdded = user.getCategoriesOfInterest().stream().anyMatch(interest -> interest.getId().equals(categoryId));

        if (!alreadyAdded) {
            user.getCategoriesOfInterest().add(category);
            userService.save(user);
        }
    }

    @Transactional
    public void removeInterest(Long categoryId) {
        User user = currentUserService.get();

        boolean removed = user.getCategoriesOfInterest().removeIf(interest -> interest.getId().equals(categoryId));

        if (removed) {
            userService.save(user);
        }
    }

    @Transactional(readOnly = true)
    public List<Item> getRecommendations() {
        User user = currentUserService.get();

        return itemService.findAvailableByCategoriesExcludingUser(user.getCategoriesOfInterest(), user);
    }
}
