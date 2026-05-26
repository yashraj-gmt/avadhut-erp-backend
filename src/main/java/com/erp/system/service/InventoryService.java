// com/erp/system/service/InventoryService.java
package com.erp.system.service;

import com.erp.system.dto.response.InventoryResponse;
import com.erp.system.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryService {

    PagedResponse<InventoryResponse> getAll(Long warehouseId,
                                            Long productId,
                                            String search,
                                            Pageable pageable);

    InventoryResponse getById(Long id);

    PagedResponse<InventoryResponse> getByWarehouse(Long warehouseId, Pageable pageable);

    List<InventoryResponse> getByProduct(Long productId);

    PagedResponse<InventoryResponse> getLowStockItems(Long warehouseId, Pageable pageable);

    PagedResponse<InventoryResponse> getItemsBelowThreshold(Double threshold, Pageable pageable);
}