package com.example.proyectobackendswaplt.item.domain;

import com.example.proyectobackendswaplt.item.infrastructure.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Item create(Item item) {
        item.setState(ItemState.AVAILABLE);
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
