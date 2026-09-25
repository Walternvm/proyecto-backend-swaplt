package com.example.proyectobackendswaplt.category.domain;

import com.example.proyectobackendswaplt.category.dto.CategoryMapper;
import com.example.proyectobackendswaplt.category.dto.CategoryRequestDto;
import com.example.proyectobackendswaplt.category.infrastructure.CategoryRepository;
import com.example.proyectobackendswaplt.common.exception.ConflictException;
import com.example.proyectobackendswaplt.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Category create(CategoryRequestDto request) {
        String normalizedName = request.getName().trim();

        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new ConflictException("La categoria ya existe");
        }

        Category category = categoryMapper.toEntity(request);

        try {
            return categoryRepository.saveAndFlush(category);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("La categoria ya existe");
        }
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        Category category = findById(id);

        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("No se puede eliminar una categoria que esta siendo utilizada");
        }
    }
}
