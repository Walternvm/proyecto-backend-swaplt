package com.example.proyectobackendswaplt.item.domain;

import com.example.proyectobackendswaplt.item.infrastructure.ItemRepository;
import com.example.proyectobackendswaplt.item.dto.ItemRequest;
import com.example.proyectobackendswaplt.category.infrastructure.CategoryRepository;
import com.example.proyectobackendswaplt.user.infrastructure.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public Item create(ItemRequest request, String email) {
        Item item = new Item();
        item.setName(request.name());
        item.setDescription(request.description());
        item.setLocation(request.location());
        item.setUser(userRepository.findByEmail(email).orElseThrow());
        item.setCategory(categoryRepository.findById(request.categoryId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found")));
        return itemRepository.save(item);
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
    }

    public void delete(Long id) {
        itemRepository.deleteById(id);
    }
}
