package com.example.proyectobackendswaplt.item.application;

import com.example.proyectobackendswaplt.common.dto.PageResponse;
import com.example.proyectobackendswaplt.item.domain.ItemCondition;
import com.example.proyectobackendswaplt.item.domain.ItemService;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.item.dto.ItemFilter;
import com.example.proyectobackendswaplt.item.dto.ItemRequest;
import com.example.proyectobackendswaplt.item.dto.ItemResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest request,
                                               Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ItemResponse.from(itemService.create(request, authentication.getName())));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ItemResponse>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ItemState state,
            @RequestParam(required = false) ItemCondition condition,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ItemFilter filter = new ItemFilter(categoryId, state, condition, location, q);
        return ResponseEntity.ok(PageResponse.from(itemService.search(filter, page, size), ItemResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ItemResponse.from(itemService.findById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        itemService.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}