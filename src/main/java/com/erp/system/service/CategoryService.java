// com/erp/system/service/CategoryService.java
package com.erp.system.service;

import com.erp.system.dto.request.CreateCategoryRequest;
import com.erp.system.dto.request.UpdateCategoryRequest;
import com.erp.system.dto.response.CategoryResponse;
import com.erp.system.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    CategoryResponse create(CreateCategoryRequest request);

    PagedResponse<CategoryResponse> getAll(String search, Boolean isActive, Pageable pageable);

    CategoryResponse getById(Long id);

    CategoryResponse update(Long id, UpdateCategoryRequest request);

    void delete(Long id);
}