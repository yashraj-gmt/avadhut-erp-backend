// com/erp/system/service/ProductService.java
package com.erp.system.service;

import com.erp.system.dto.request.CreateProductRequest;
import com.erp.system.dto.request.UpdateProductRequest;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.dto.response.ProductResponse;
import com.erp.system.dto.response.ProductSummaryResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponse create(CreateProductRequest request, List<MultipartFile> images);

    PagedResponse<ProductSummaryResponse> getAll(String search, Boolean isActive, Pageable pageable);

    ProductResponse getById(Long id);

    ProductResponse update(Long id, UpdateProductRequest request, List<MultipartFile> newImages);

    /** Soft-delete. Also marks related images as deleted. */
    void delete(Long id);

    /** Soft-delete a single product image. */
    void deleteImage(Long productId, Long imageId);
}