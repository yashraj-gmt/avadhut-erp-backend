// com/erp/system/service/impl/InventoryServiceImpl.java
package com.erp.system.service.impl;

import com.erp.system.dto.response.InventoryResponse;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.entity.Inventory;
import com.erp.system.exception.ResourceNotFoundException;
import com.erp.system.mapper.InventoryMapper;
import com.erp.system.repository.InventoryRepository;
import com.erp.system.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper     inventoryMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InventoryResponse> getAll(Long warehouseId, Long productId,
                                                   String search, Pageable pageable) {
        Page<Inventory> page =
                inventoryRepository.findAllWithFilters(warehouseId, productId, search, pageable);
        return PagedResponse.from(page.map(inventoryMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getById(Long id) {
        Inventory inv = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory record not found: " + id));
        return inventoryMapper.toResponse(inv);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InventoryResponse> getByWarehouse(Long warehouseId, Pageable pageable) {
        Page<Inventory> page = inventoryRepository.findByWarehouseId(warehouseId, pageable);
        return PagedResponse.from(page.map(inventoryMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getByProduct(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InventoryResponse> getLowStockItems(Long warehouseId, Pageable pageable) {
        Page<Inventory> page = inventoryRepository.findLowStockItems(warehouseId, pageable);
        return PagedResponse.from(page.map(inventoryMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InventoryResponse> getItemsBelowThreshold(Double threshold, Pageable pageable) {
        Page<Inventory> page =
                inventoryRepository.findItemsBelowAlertThreshold(threshold, pageable);
        return PagedResponse.from(page.map(inventoryMapper::toResponse));
    }
}