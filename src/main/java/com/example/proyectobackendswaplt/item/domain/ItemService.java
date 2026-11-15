package com.example.proyectobackendswaplt.item.domain;

import com.example.proyectobackendswaplt.auth.CurrentUserService;
import com.example.proyectobackendswaplt.category.domain.Category;
import com.example.proyectobackendswaplt.category.domain.CategoryService;
import com.example.proyectobackendswaplt.common.exception.ConflictException;
import com.example.proyectobackendswaplt.common.exception.ForbiddenException;
import com.example.proyectobackendswaplt.common.exception.ResourceNotFoundException;
import com.example.proyectobackendswaplt.item.dto.ItemFilter;
import com.example.proyectobackendswaplt.item.dto.ItemMapper;
import com.example.proyectobackendswaplt.item.dto.ItemRequestDto;
import com.example.proyectobackendswaplt.item.infrastructure.ItemRepository;
import com.example.proyectobackendswaplt.item.infrastructure.ItemSpecifications;
import com.example.proyectobackendswaplt.user.domain.User;
import com.example.proyectobackendswaplt.item.infrastructure.ItemImageStorageService;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private static final int MAX_PAGE_SIZE = 100;

    private final ItemRepository itemRepository;
    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;
    private final ItemMapper itemMapper;
    private final ItemImageStorageService itemImageStorageService;

    @Transactional
    public Item create(ItemRequestDto request) {
        Item item = itemMapper.toEntity(request);

        item.setUser(currentUserService.get());
        item.setCategory(categoryService.findById(request.getCategoryId()));
        item.setState(ItemState.AVAILABLE);

        return itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public Page<Item> search(ItemFilter filter, int page, int size) {
        int safePage = Math.max(page, 0);

        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return itemRepository.findAll(ItemSpecifications.matching(filter), pageable);
    }

    @Transactional(readOnly = true)
    public Item findById(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Item", id));
    }

    @Transactional
    public Item update(Long id, ItemRequestDto request) {
        Item item = findById(id);
        User currentUser = currentUserService.get();

        if (!item.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException(
                    "Solo el propietario puede editar el item"
            );
        }

        if (item.getState() != ItemState.AVAILABLE) {
            throw new ConflictException(
                    "No se puede editar un item reservado o intercambiado"
            );
        }

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(
                categoryService.findById(request.getCategoryId())
        );
        item.setLocation(request.getLocation());
        item.setLatitude(request.getLatitude());
        item.setLongitude(request.getLongitude());
        item.setWantedItem(request.getWantedItem());
        item.setCondition(request.getCondition());

        return itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<Item> findMine() {
        return itemRepository.findByUserOrderByCreatedAtDesc(
                currentUserService.get()
        );
    }

    @Transactional
    public Item updateImage(Long id, MultipartFile image) {
        Item item = findById(id);
        User currentUser = currentUserService.get();

        if (!item.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException(
                    "Solo el propietario puede modificar la imagen"
            );
        }

        if (item.getState() != ItemState.AVAILABLE) {
            throw new ConflictException(
                    "No se puede modificar la imagen de un item reservado o intercambiado"
            );
        }

        String previousImageKey = item.getImageKey();
        String newImageKey =
                itemImageStorageService.upload(id, image);

        try {
            item.setImageKey(newImageKey);

            Item saved = itemRepository.saveAndFlush(item);

            itemImageStorageService.delete(previousImageKey);

            return saved;
        } catch (RuntimeException exception) {
            try {
                itemImageStorageService.delete(newImageKey);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }

            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public String getImageUrl(Long id) {
        Item item = findById(id);

        if (item.getImageKey() == null
                || item.getImageKey().isBlank()) {
            throw new ResourceNotFoundException(
                    "El item no tiene una imagen"
            );
        }

        return itemImageStorageService.createReadUrl(
                item.getImageKey()
        );
    }

    @Transactional(readOnly = true)
    public List<Item> findAvailableByCategoriesExcludingUser(Collection<Category> categories, User user) {
        if (categories.isEmpty()) {
            return List.of();
        }

        return itemRepository.findByStateAndCategoryInAndUserNotOrderByCreatedAtDesc(ItemState.AVAILABLE, categories, user);
    }

    @Transactional
    public Item markReserved(Item item) {
        return changeState(item, ItemState.AVAILABLE, ItemState.RESERVED);
    }

    @Transactional
    public Item markAvailable(Item item) {
        return changeState(item, ItemState.RESERVED, ItemState.AVAILABLE);
    }

    @Transactional
    public Item markTraded(Item item) {
        return changeState(item, ItemState.RESERVED, ItemState.TRADED);
    }

    @Transactional
    public void delete(Long id) {
        Item item = findById(id);
        User currentUser = currentUserService.get();

        if (!item.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Solo el propietario puede eliminar el item");
        }

        if (item.getState() != ItemState.AVAILABLE) {
            throw new ConflictException("No se puede eliminar un item reservado o intercambiado");
        }

        String imageKey = item.getImageKey();

        itemRepository.delete(item);
        itemRepository.flush();

        itemImageStorageService.delete(imageKey);
    }

    private Item changeState(Item item, ItemState expectedState, ItemState newState) {
        if (item.getState() != expectedState) {
            throw new ConflictException("El item no se encuentra en el estado requerido");
        }

        item.setState(newState);

        return itemRepository.save(item);
    }
}
