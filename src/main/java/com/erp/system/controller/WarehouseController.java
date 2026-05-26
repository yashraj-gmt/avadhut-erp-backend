package com.erp.system.controller;

import com.erp.system.dto.request.CreateWarehouseRequest;
import com.erp.system.dto.request.UpdateWarehouseRequest;
import com.erp.system.dto.response.ApiResponse;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.dto.response.WarehouseResponse;
import com.erp.system.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    // ── POST /api/inventory/warehouses ───────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseResponse>> create(
            @Valid @RequestBody CreateWarehouseRequest request) {

        WarehouseResponse data = warehouseService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Warehouse created successfully.", data));
    }

    // ── GET /api/inventory/warehouses ────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<WarehouseResponse>>> getAll(
            @RequestParam(required = false)           String  search,
            @RequestParam(required = false)           Boolean isActive,
            @RequestParam(defaultValue = "0")         int     page,
            @RequestParam(defaultValue = "20")        int     size,
            @RequestParam(defaultValue = "createdAt") String  sortBy,
            @RequestParam(defaultValue = "desc")      String  sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PagedResponse<WarehouseResponse> data =
                warehouseService.getAll(search, isActive, PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.success("Warehouses retrieved successfully.", data));
    }

    // ── GET /api/inventory/warehouses/{id} ───────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Warehouse retrieved successfully.", warehouseService.getById(id))
        );
    }

    // ── PATCH /api/inventory/warehouses/{id} ─────────────────────────────
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWarehouseRequest request) {

        WarehouseResponse data = warehouseService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Warehouse updated successfully.", data));
    }

    // ── DELETE /api/inventory/warehouses/{id} ────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Warehouse deleted successfully."));
    }
}