// com/erp/system/service/WarehouseService.java
package com.erp.system.service;

import com.erp.system.dto.request.CreateWarehouseRequest;
import com.erp.system.dto.request.UpdateWarehouseRequest;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.dto.response.WarehouseResponse;
import org.springframework.data.domain.Pageable;

public interface WarehouseService {

    WarehouseResponse create(CreateWarehouseRequest request);

    PagedResponse<WarehouseResponse> getAll(String search, Boolean isActive, Pageable pageable);

    WarehouseResponse getById(Long id);

    WarehouseResponse update(Long id, UpdateWarehouseRequest request);

    /** Soft-delete. */
    void delete(Long id);
}