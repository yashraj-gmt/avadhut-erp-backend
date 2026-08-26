package com.erp.system.controller;

import com.erp.system.dto.request.CreateGeneratorRequest;
import com.erp.system.dto.request.UpdateGeneratorRequest;
import com.erp.system.dto.response.ApiResponse;
import com.erp.system.dto.response.GeneratorResponse;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.service.GeneratorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for Generator Management endpoints: /api/admin/generators
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/generators")
@CrossOrigin
@RequiredArgsConstructor
public class GeneratorController {

    private final GeneratorService generatorService;
    private final ObjectMapper objectMapper;

    // ── GET /api/admin/generators ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<GeneratorResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PagedResponse<GeneratorResponse> data =
                generatorService.getAll(search, isActive, PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.success("Generators retrieved successfully.", data));
    }

    // ── GET /api/admin/generators/availability ─────────────────────────────────
    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<List<GeneratorResponse>>> getAvailability(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long excludeOrderId) {

        List<GeneratorResponse> data =
                generatorService.getForDropdownWithAvailability(startDate, endDate, excludeOrderId);

        return ResponseEntity.ok(ApiResponse.success("Generator availability retrieved successfully.", data));
    }

    // ── GET /api/admin/generators/availability/daily-all ──────────────────────
    @GetMapping("/availability/daily-all")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDailyAvailabilityAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<Map<String, Object>> data = generatorService.getDailyAvailabilityAll(targetDate);

        return ResponseEntity.ok(ApiResponse.success("Daily availability retrieved successfully.", data));
    }

    // ── GET /api/admin/generators/{id} ─────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GeneratorResponse>> getById(@PathVariable Long id) {
        GeneratorResponse data = generatorService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Generator retrieved successfully.", data));
    }

    // ── GET /api/admin/generators/{id}/daily-availability ─────────────────────
    @GetMapping("/{id}/daily-availability")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDailyAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Map<String, Object>> data = generatorService.getDailyAvailability(id, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Generator daily availability retrieved successfully.", data));
    }

    // ── POST /api/admin/generators ────────────────────────────────────────────
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GeneratorResponse>> create(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {

        CreateGeneratorRequest request = objectMapper.readValue(dataJson, CreateGeneratorRequest.class);
        GeneratorResponse data = generatorService.create(request, image);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Generator created successfully.", data));
    }

    // ── PATCH /api/admin/generators/{id} ──────────────────────────────────────
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<GeneratorResponse>> update(
            @PathVariable Long id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {

        UpdateGeneratorRequest request = objectMapper.readValue(dataJson, UpdateGeneratorRequest.class);
        GeneratorResponse data = generatorService.update(id, request, image);

        return ResponseEntity.ok(ApiResponse.success("Generator updated successfully.", data));
    }

    // ── DELETE /api/admin/generators/{id} ──────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        generatorService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Generator deleted successfully."));
    }
}
