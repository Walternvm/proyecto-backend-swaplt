package com.example.proyectobackendswaplt.item.application;

import com.example.proyectobackendswaplt.common.dto.PageResponseDto;
import com.example.proyectobackendswaplt.item.domain.ItemCondition;
import com.example.proyectobackendswaplt.item.domain.ItemService;
import com.example.proyectobackendswaplt.item.domain.ItemState;
import com.example.proyectobackendswaplt.item.dto.ItemFilter;
import com.example.proyectobackendswaplt.item.dto.ItemMapper;
import com.example.proyectobackendswaplt.item.dto.ItemRequestDto;
import com.example.proyectobackendswaplt.item.dto.ItemResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @PostMapping
    public ResponseEntity<ItemResponseDto> create(@Valid @RequestBody ItemRequestDto request) {
        ItemResponseDto response = itemMapper.toResponseDto(itemService.create(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponseDto<ItemResponseDto>> search(
            @RequestParam(required = false)
            Long categoryId,

            @RequestParam(required = false)
            ItemState state,

            @RequestParam(required = false)
            ItemCondition condition,

            @RequestParam(required = false)
            String location,

            @RequestParam(required = false)
            String q,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size
    ) {
        ItemFilter filter = new ItemFilter(categoryId, state, condition, location, q);

        PageResponseDto<ItemResponseDto> response = PageResponseDto.from(itemService.search(filter, page, size), itemMapper::toResponseDto);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> findById(@PathVariable Long id) {
        ItemResponseDto response = itemMapper.toResponseDto(itemService.findById(id));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
