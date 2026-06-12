package com.erp.system.controller;

import com.erp.system.dto.request.CreateGeneratorRequest;
import com.erp.system.dto.request.UpdateGeneratorRequest;
import com.erp.system.dto.response.ApiResponse;
import com.erp.system.dto.response.GeneratorResponse;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.enums.GeneratorFuelType;
import com.erp.system.enums.GeneratorStatus;
import com.erp.system.service.GeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/generators")
@RequiredArgsConstructor
public class GeneratorController {

    private final GeneratorService generatorService;

    // ── POST /api/admin/generators ───────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<GeneratorResponse>> create(
            @Valid @RequestBody CreateGeneratorRequest request) {

        GeneratorResponse data = generatorService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Generator created successfully.", data));
    }

    // ── GET /api/admin/generators ────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<GeneratorResponse>>> getAll(
            @RequestParam(required = false)                String              search,
            @RequestParam(required = false)                GeneratorStatus     currentStatus,
            @RequestParam(required = false)                GeneratorFuelType   fuelType,
            @RequestParam(required = false)                Boolean             isActive,
            @RequestParam(defaultValue = "0")              int                 page,
            @RequestParam(defaultValue = "20")             int                 size,
            @RequestParam(defaultValue = "createdAt")      String              sortBy,
            @RequestParam(defaultValue = "desc")           String              sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PagedResponse<GeneratorResponse> data =
                generatorService.getAll(search, currentStatus, fuelType, isActive,
                        PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.success("Generators retrieved successfully.", data));
    }

    // ── GET /api/admin/generators/{id} ───────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GeneratorResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Generator retrieved successfully.", generatorService.getById(id))
        );
    }

    // ── PATCH /api/admin/generators/{id} ─────────────────────────────────
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<GeneratorResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGeneratorRequest request) {

        GeneratorResponse data = generatorService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Generator updated successfully.", data));
    }

    // ── DELETE /api/admin/generators/{id} ─────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        generatorService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Generator deleted successfully."));
    }
}