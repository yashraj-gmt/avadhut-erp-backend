// com/erp/system/service/ProductService.java
package com.erp.system.service;

import com.erp.system.dto.request.CreateProductRequest;
import com.erp.system.dto.request.UpdateProductRequest;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.dto.response.ProductResponse;
import com.erp.system.dto.response.ProductSummaryResponse;
import com.erp.system.enums.ProductStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    ProductResponse create(CreateProductRequest request,
                           List<MultipartFile> images,
                           MultipartFile qrCode);

    PagedResponse<ProductSummaryResponse> getAll(String search,
                                                 Long categoryId,
                                                 ProductStatus status,
                                                 Boolean isActive,
                                                 Long warehouseId,
                                                 Pageable pageable);

    ProductResponse getById(Long id);

    ProductResponse update(Long id,
                           UpdateProductRequest request,
                           List<MultipartFile> newImages,
                           MultipartFile newQrCode);

    ProductResponse publish(Long id);

    ProductResponse revertToDraft(Long id);

    /** Soft-delete. Also marks related images as deleted. */
    void delete(Long id);

    /** Soft-delete a single product image. */
    void deleteImage(Long productId, Long imageId);
}