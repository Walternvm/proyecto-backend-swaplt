package com.example.proyectobackendswaplt.item.domain;

import com.example.proyectobackendswaplt.category.domain.CategoryService;
import com.example.proyectobackendswaplt.common.exception.ForbiddenException;
import com.example.proyectobackendswaplt.common.exception.ResourceNotFoundException;
import com.example.proyectobackendswaplt.item.dto.ItemFilter;
import com.example.proyectobackendswaplt.item.dto.ItemRequest;
import com.example.proyectobackendswaplt.item.infrastructure.ItemRepository;
import com.example.proyectobackendswaplt.item.infrastructure.ItemSpecifications;
import com.example.proyectobackendswaplt.user.domain.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {
    private static final int MAX_PAGE_SIZE = 100;

    private final ItemRepository itemRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    public Item create(ItemRequest request, String email) {
        Item item = new Item();
        item.setName(request.name());
        item.setDescription(request.description());
        item.setLocation(request.location());
        item.setCondition(request.condition());
        item.setUser(userService.getByEmail(email));
        item.setCategory(categoryService.findById(request.categoryId()));
        return itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public Page<Item> search(ItemFilter filter, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "id"));
        return itemRepository.findAll(ItemSpecifications.matching(filter), pageable);
    }

    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
    }

    public Item markReserved(Item item) {
        item.setState(ItemState.RESERVED);
        return itemRepository.save(item);
    }

    @Transactional
    public void delete(Long id, String email) {
        Item item = findById(id);
        if (!item.getUser().getEmail().equals(email)) {
            throw new ForbiddenException();
        }
        itemRepository.delete(item);
    }
}