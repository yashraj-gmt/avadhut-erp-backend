package com.erp.system.controller;

import com.erp.system.dto.request.CreateProductRequest;
import com.erp.system.dto.request.UpdateProductRequest;
import com.erp.system.dto.response.ApiResponse;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.dto.response.ProductResponse;
import com.erp.system.dto.response.ProductSummaryResponse;
import com.erp.system.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Product REST controller.
 *
 * All create / update endpoints use multipart/form-data:
 *
 *   Part "data"   — JSON (CreateProductRequest / UpdateProductRequest)
 *                   Content-Type: application/json
 *   Part "images" — zero or more image files
 *
 * Postman example:
 *   POST /api/inventory/products
 *   Body → form-data
 *     key: data    | type: Text (application/json)  | value: { "name": "...", ... }
 *     key: images  | type: File (multipart)          | value: <image files>
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/products")
@CrossOrigin()
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper   objectMapper;

    // ── POST /api/inventory/products ─────────────────────────────────────
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @RequestPart("data")                           String              dataJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images)
            throws Exception {

        CreateProductRequest request = objectMapper.readValue(dataJson, CreateProductRequest.class);
        validateRequest(request);

        ProductResponse data = productService.create(request, images);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully.", data));
    }

    // ── GET /api/inventory/products ──────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductSummaryResponse>>> getAll(
            @RequestParam(required = false)           String  search,
            @RequestParam(required = false)           Boolean isActive,
            @RequestParam(defaultValue = "0")         int     page,
            @RequestParam(defaultValue = "20")        int     size,
            @RequestParam(defaultValue = "createdAt") String  sortBy,
            @RequestParam(defaultValue = "desc")      String  sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PagedResponse<ProductSummaryResponse> data =
                productService.getAll(search, isActive, PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully.", data));
    }

    // ── GET /api/inventory/products/{id} ─────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Product retrieved successfully.", productService.getById(id))
        );
    }

    // ── PATCH /api/inventory/products/{id}
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @RequestPart("data")                           String              dataJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> newImages)
            throws Exception {

        UpdateProductRequest request = objectMapper.readValue(dataJson, UpdateProductRequest.class);

        ProductResponse data = productService.update(id, request, newImages);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully.", data));
    }

    // ── DELETE /api/inventory/products/{id} ──────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully."));
    }

    // ── DELETE /api/inventory/products/{id}/images/{imageId} ─────────────
    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long id,
            @PathVariable Long imageId) {

        productService.deleteImage(id, imageId);
        return ResponseEntity.ok(ApiResponse.success("Product image deleted successfully."));
    }

    // ── Manual bean validation (since @Valid can't be used on raw String part) ──
    private void validateRequest(CreateProductRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (req.getProductCode() == null || req.getProductCode().isBlank()) {
            throw new IllegalArgumentException("Product code is required.");
        }
    }
}