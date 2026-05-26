// com/erp/system/service/impl/WarehouseServiceImpl.java
package com.erp.system.service.impl;

import com.erp.system.dto.request.CreateWarehouseRequest;
import com.erp.system.dto.request.UpdateWarehouseRequest;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.dto.response.WarehouseResponse;
import com.erp.system.entity.Warehouse;
import com.erp.system.exception.AppException;
import com.erp.system.exception.ResourceNotFoundException;
import com.erp.system.mapper.WarehouseMapper;
import com.erp.system.repository.WarehouseRepository;
import com.erp.system.service.WarehouseService;
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
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper     warehouseMapper;

    // ── Create ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WarehouseResponse create(CreateWarehouseRequest request) {
        validateUniqueOnCreate(request.getName(), request.getCode());

        Warehouse warehouse = new Warehouse();
        applyFields(warehouse, request);
        warehouse.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        Warehouse saved = warehouseRepository.save(warehouse);
        log.info("Warehouse created: id={}, code={}", saved.getId(), saved.getCode());
        return buildResponseWithStats(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<WarehouseResponse> getAll(String search, Boolean isActive, Pageable pageable) {
        Page<Warehouse> page = warehouseRepository.findAllWithFilters(search, isActive, pageable);
        return PagedResponse.from(page.map(this::buildResponseWithStats));
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getById(Long id) {
        return buildResponseWithStats(findOrThrow(id));
    }

    // ── Update ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WarehouseResponse update(Long id, UpdateWarehouseRequest request) {
        Warehouse warehouse = findOrThrow(id);

        if (request.getName() != null) {
            if (warehouseRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
                throw new AppException(
                        "Warehouse name '" + request.getName() + "' is already taken.",
                        HttpStatus.CONFLICT
                );
            }
            warehouse.setName(request.getName().trim());
        }
        if (request.getCode() != null) {
            String code = request.getCode().toUpperCase();
            if (warehouseRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
                throw new AppException("Warehouse code '" + code + "' is already in use.",
                        HttpStatus.CONFLICT);
            }
            warehouse.setCode(code);
        }
        if (request.getLocation()      != null) warehouse.setLocation(request.getLocation());
        if (request.getAddress()       != null) warehouse.setAddress(request.getAddress());
        if (request.getCity()          != null) warehouse.setCity(request.getCity());
        if (request.getState()         != null) warehouse.setState(request.getState());
        if (request.getPincode()       != null) warehouse.setPincode(request.getPincode());
        if (request.getContactPerson() != null) warehouse.setContactPerson(request.getContactPerson());
        if (request.getContactPhone()  != null) warehouse.setContactPhone(request.getContactPhone());
        if (request.getTotalCapacity() != null) warehouse.setTotalCapacity(request.getTotalCapacity());
        if (request.getIsActive()      != null) warehouse.setIsActive(request.getIsActive());

        Warehouse saved = warehouseRepository.save(warehouse);
        log.info("Warehouse updated: id={}", saved.getId());
        return buildResponseWithStats(saved);
    }

    // ── Soft-Delete ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        Warehouse warehouse = findOrThrow(id);

        long productCount = warehouseRepository.countProductsByWarehouseId(id);
        if (productCount > 0) {
            throw new AppException(
                    "Cannot delete warehouse — it has " + productCount
                            + " product inventory record(s). Remove all inventory records before deleting.",
                    HttpStatus.CONFLICT
            );
        }

        warehouse.setDeleted(true);         // ← soft-delete
        warehouseRepository.save(warehouse);
        log.info("Warehouse soft-deleted: id={}", id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Warehouse findOrThrow(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
    }

    private WarehouseResponse buildResponseWithStats(Warehouse w) {
        WarehouseResponse response = warehouseMapper.toResponse(w);
        response.setTotalProducts(warehouseRepository.countProductsByWarehouseId(w.getId()));
        response.setTotalStockQuantity(warehouseRepository.sumStockByWarehouseId(w.getId()));
        return response;
    }

    private void validateUniqueOnCreate(String name, String code) {
        if (warehouseRepository.existsByNameIgnoreCase(name)) {
            throw new AppException("Warehouse name '" + name + "' already exists.", HttpStatus.CONFLICT);
        }
        if (warehouseRepository.existsByCodeIgnoreCase(code)) {
            throw new AppException("Warehouse code '" + code + "' already exists.", HttpStatus.CONFLICT);
        }
    }

    private void applyFields(Warehouse w, CreateWarehouseRequest req) {
        w.setName(req.getName().trim());
        w.setCode(req.getCode().toUpperCase().trim());
        w.setLocation(req.getLocation());
        w.setAddress(req.getAddress());
        w.setCity(req.getCity());
        w.setState(req.getState());
        w.setPincode(req.getPincode());
        w.setContactPerson(req.getContactPerson());
        w.setContactPhone(req.getContactPhone());
        w.setTotalCapacity(req.getTotalCapacity());
    }
}