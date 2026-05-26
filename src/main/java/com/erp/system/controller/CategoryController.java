package com.erp.system.controller;

import com.erp.system.dto.request.CreateCategoryRequest;
import com.erp.system.dto.request.UpdateCategoryRequest;
import com.erp.system.dto.response.ApiResponse;
import com.erp.system.dto.response.CategoryResponse;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // ── POST /api/inventory/categories ───────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CreateCategoryRequest request) {

        CategoryResponse data = categoryService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully.", data));
    }

    // ── GET /api/inventory/categories ────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CategoryResponse>>> getAll(
            @RequestParam(required = false)                String  search,
            @RequestParam(required = false)                Boolean isActive,
            @RequestParam(defaultValue = "0")              int     page,
            @RequestParam(defaultValue = "20")             int     size,
            @RequestParam(defaultValue = "createdAt")      String  sortBy,
            @RequestParam(defaultValue = "desc")           String  sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PagedResponse<CategoryResponse> data =
                categoryService.getAll(search, isActive, PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully.", data));
    }

    // ── GET /api/inventory/categories/{id} ───────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Category retrieved successfully.", categoryService.getById(id))
        );
    }

    // ── PATCH /api/inventory/categories/{id} ─────────────────────────────
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {

        CategoryResponse data = categoryService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully.", data));
    }

    // ── DELETE /api/inventory/categories/{id} ────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully."));
    }
}