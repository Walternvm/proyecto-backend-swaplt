package com.example.proyectobackendswaplt.item.domain;

import com.example.proyectobackendswaplt.category.domain.CategoryService;
import com.example.proyectobackendswaplt.common.exception.ForbiddenException;
import com.example.proyectobackendswaplt.common.exception.ResourceNotFoundException;
import com.example.proyectobackendswaplt.item.dto.ItemRequest;
import com.example.proyectobackendswaplt.item.infrastructure.ItemRepository;
import com.example.proyectobackendswaplt.user.domain.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    public Item create(ItemRequest request, String email) {
        Item item = new Item();
        item.setName(request.name());
        item.setDescription(request.description());
        item.setLocation(request.location());
        item.setUser(userService.getByEmail(email));
        item.setCategory(categoryService.findById(request.categoryId()));
        return itemRepository.save(item);
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
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