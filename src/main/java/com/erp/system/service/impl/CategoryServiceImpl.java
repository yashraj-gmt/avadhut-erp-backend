// com/erp/system/service/impl/CategoryServiceImpl.java
package com.erp.system.service.impl;

import com.erp.system.dto.request.CreateCategoryRequest;
import com.erp.system.dto.request.UpdateCategoryRequest;
import com.erp.system.dto.response.CategoryResponse;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.entity.ProductCategory;
import com.erp.system.exception.AppException;
import com.erp.system.exception.ResourceNotFoundException;
import com.erp.system.mapper.CategoryMapper;
import com.erp.system.repository.CategoryRepository;
import com.erp.system.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper     categoryMapper;

    // ── Create ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(
                    "Category with name '" + request.getName() + "' already exists.",
                    HttpStatus.CONFLICT
            );
        }

        ProductCategory category = new ProductCategory();
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        ProductCategory saved = categoryRepository.save(category);
        log.info("Category created: id={}, name={}", saved.getId(), saved.getName());

        CategoryResponse response = categoryMapper.toResponse(saved);
        response.setProductCount(0L);
        return response;
    }

    // ── Read ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CategoryResponse> getAll(String search, Boolean isActive, Pageable pageable) {
        Page<ProductCategory> page =
                categoryRepository.findAllWithFilters(search, isActive, pageable);

        return PagedResponse.from(page.map(c -> {
            CategoryResponse r = categoryMapper.toResponse(c);
            r.setProductCount(categoryRepository.countActiveProductsByCategoryId(c.getId()));
            return r;
        }));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        ProductCategory category = findOrThrow(id);
        CategoryResponse response = categoryMapper.toResponse(category);
        response.setProductCount(categoryRepository.countActiveProductsByCategoryId(id));
        return response;
    }

    // ── Update ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        ProductCategory category = findOrThrow(id);

        if (request.getName() != null) {
            String newName = request.getName().trim();
            if (categoryRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {
                throw new AppException(
                        "Category name '" + newName + "' is already taken.",
                        HttpStatus.CONFLICT
                );
            }
            category.setName(newName);
        }
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getIsActive()    != null) category.setIsActive(request.getIsActive());

        ProductCategory saved = categoryRepository.save(category);
        log.info("Category updated: id={}", saved.getId());

        CategoryResponse response = categoryMapper.toResponse(saved);
        response.setProductCount(categoryRepository.countActiveProductsByCategoryId(id));
        return response;
    }

    // ── Soft-Delete ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        ProductCategory category = findOrThrow(id);

        long productCount = categoryRepository.countActiveProductsByCategoryId(id);
        if (productCount > 0) {
            throw new AppException(
                    "Cannot delete category — it has " + productCount + " active product(s) linked to it.",
                    HttpStatus.CONFLICT
            );
        }

        category.setDeleted(true);          // ← soft-delete
        categoryRepository.save(category);
        log.info("Category soft-deleted: id={}", id);
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private ProductCategory findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}