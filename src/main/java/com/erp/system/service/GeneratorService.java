package com.erp.system.service;

import com.erp.system.dto.request.CreateGeneratorRequest;
import com.erp.system.dto.request.UpdateGeneratorRequest;
import com.erp.system.dto.response.GeneratorResponse;
import com.erp.system.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface GeneratorService {

    /** Create a generator; optionally attach a single image file. */
    GeneratorResponse create(CreateGeneratorRequest request, MultipartFile image);

    PagedResponse<GeneratorResponse> getAll(String search, Boolean isActive, Pageable pageable);

    GeneratorResponse getById(Long id);

    /** Update a generator; optionally replace its image with a new file. */
    GeneratorResponse update(Long id, UpdateGeneratorRequest request, MultipartFile image);

    void delete(Long id);

    List<GeneratorResponse> getForDropdownWithAvailability(LocalDate startDate, LocalDate endDate, Long excludeOrderId);

    /**
     * Returns a per-date availability breakdown for a single generator.
     * Each entry in the returned list contains:
     *   date, totalStock, underServiceQty, bookedQty, availableQty
     */
    List<Map<String, Object>> getDailyAvailability(Long generatorId, LocalDate startDate, LocalDate endDate);

    /**
     * Returns availability breakdown for all active generators for a specific date.
     */
    List<Map<String, Object>> getDailyAvailabilityAll(LocalDate date);
}
