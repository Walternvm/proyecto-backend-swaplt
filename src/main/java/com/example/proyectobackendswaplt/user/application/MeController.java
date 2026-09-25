package com.example.proyectobackendswaplt.user.application;

import com.example.proyectobackendswaplt.auth.CurrentUserService;
import com.example.proyectobackendswaplt.category.dto.CategoryMapper;
import com.example.proyectobackendswaplt.category.dto.CategoryResponseDto;
import com.example.proyectobackendswaplt.item.dto.ItemMapper;
import com.example.proyectobackendswaplt.item.dto.ItemResponseDto;
import com.example.proyectobackendswaplt.user.domain.UserPreferenceService;
import com.example.proyectobackendswaplt.user.dto.UserMapper;
import com.example.proyectobackendswaplt.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class MeController {
    private final CurrentUserService currentUserService;
    private final UserPreferenceService userPreferenceService;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<UserResponseDto> me() {
        UserResponseDto response = userMapper.toResponseDto(currentUserService.get());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<ItemResponseDto>> getFavorites() {
        List<ItemResponseDto> response = userPreferenceService.getFavorites().stream().map(itemMapper::toResponseDto).toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/favorites/{itemId}")
    public ResponseEntity<Void> addFavorite(@PathVariable Long itemId) {
        userPreferenceService.addFavorite(itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/favorites/{itemId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long itemId) {
        userPreferenceService.removeFavorite(itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/interests")
    public ResponseEntity<List<CategoryResponseDto>> getInterests() {
        List<CategoryResponseDto> response = userPreferenceService.getInterests().stream().map(categoryMapper::toResponseDto).toList();
        return ResponseEntity.ok(response);
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
    public ResponseEntity<List<ItemResponseDto>> getRecommendations() {
        List<ItemResponseDto> response = userPreferenceService.getRecommendations().stream().map(itemMapper::toResponseDto).toList();
        return ResponseEntity.ok(response);
    }
}
