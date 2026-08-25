package com.erp.system.service.impl;

import com.erp.system.dto.request.CreateGeneratorRequest;
import com.erp.system.dto.request.UpdateGeneratorRequest;
import com.erp.system.dto.response.GeneratorResponse;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.entity.Generator;
import com.erp.system.exception.AppException;
import com.erp.system.exception.ResourceNotFoundException;
import com.erp.system.mapper.GeneratorMapper;
import com.erp.system.repository.GeneratorRepository;
import com.erp.system.service.FileUploadService;
import com.erp.system.service.GeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.erp.system.entity.OrderItem;
import com.erp.system.entity.Order;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratorServiceImpl implements GeneratorService {

    private static final String DIR_GENERATOR_IMAGES = "generators/images";

    private final GeneratorRepository generatorRepository;
    private final GeneratorMapper     generatorMapper;
    private final FileUploadService   fileUploadService;

    // ── Create ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public GeneratorResponse create(CreateGeneratorRequest request, MultipartFile image) {
            // Validate unique name
        if (generatorRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(
                    "Generator with name '" + request.getName() + "' already exists.",
                    HttpStatus.CONFLICT
            );
        }

        Generator generator = new Generator();
        
        // Validate unique code
        if (request.getGeneratorCode() != null && !request.getGeneratorCode().trim().isEmpty()) {
            if (generatorRepository.existsByGeneratorCodeIgnoreCase(request.getGeneratorCode().trim())) {
                throw new AppException(
                        "Generator with code '" + request.getGeneratorCode() + "' already exists.",
                        HttpStatus.CONFLICT
                );
            }
            generator.setGeneratorCode(request.getGeneratorCode().trim().toUpperCase());
        } else {
            Long maxId = generatorRepository.getMaxId();
            long count = (maxId == null ? 0 : maxId) + 1;
            generator.setGeneratorCode("GEN-" + String.format("%03d", count));
        }

        generator.setName(request.getName().trim());
        generator.setPurchasePrice(request.getPurchasePrice());
        generator.setPartyDieselRentPrice(request.getPartyDieselRentPrice());
        generator.setWithDieselRentPrice(request.getWithDieselRentPrice());
        generator.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        generator.setUnderServiceQuantity(request.getUnderServiceQuantity() != null ? request.getUnderServiceQuantity() : 0);
        generator.setProductBy(request.getProductBy());
        generator.setDescription(request.getDescription());
        generator.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            String relativePath = fileUploadService.uploadImage(image, DIR_GENERATOR_IMAGES);
            generator.setImageUrl(relativePath);
        }

        Generator saved = generatorRepository.save(generator);
        log.info("Generator created: id={}, name={}, code={}",
                saved.getId(), saved.getName(), saved.getGeneratorCode());

        return toResponseWithEffectiveStock(saved);
    }

    // ── Read ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<GeneratorResponse> getAll(String search, Boolean isActive, Pageable pageable) {
        Page<Generator> page = generatorRepository.findAllWithFilters(search, isActive, pageable);
        return PagedResponse.from(page.map(this::toResponseWithEffectiveStock));
    }

    @Override
    @Transactional(readOnly = true)
    public GeneratorResponse getById(Long id) {
        Generator generator = findOrThrow(id);
        return toResponseWithEffectiveStock(generator);
    }

    // ── Update ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public GeneratorResponse update(Long id, UpdateGeneratorRequest request, MultipartFile image) {
        Generator generator = findOrThrow(id);

        // Name uniqueness check
        if (request.getName() != null) {
            String newName = request.getName().trim();
            if (generatorRepository.existsByNameIgnoreCaseAndIdNot(newName, id)) {
                throw new AppException(
                        "Generator name '" + newName + "' is already taken.",
                        HttpStatus.CONFLICT
                );
            }
            generator.setName(newName);
        }

        // Code uniqueness check
        if (request.getGeneratorCode() != null && !request.getGeneratorCode().trim().isEmpty()) {
            String newCode = request.getGeneratorCode().trim().toUpperCase();
            if (generatorRepository.existsByGeneratorCodeIgnoreCaseAndIdNot(newCode, id)) {
                throw new AppException(
                        "Generator code '" + newCode + "' is already taken.",
                        HttpStatus.CONFLICT
                );
            }
            generator.setGeneratorCode(newCode);
        }

        if (request.getPurchasePrice()        != null) generator.setPurchasePrice(request.getPurchasePrice());
        if (request.getPartyDieselRentPrice() != null) generator.setPartyDieselRentPrice(request.getPartyDieselRentPrice());
        if (request.getWithDieselRentPrice()  != null) generator.setWithDieselRentPrice(request.getWithDieselRentPrice());
        if (request.getStockQuantity()        != null) generator.setStockQuantity(request.getStockQuantity());
        if (request.getUnderServiceQuantity() != null) generator.setUnderServiceQuantity(request.getUnderServiceQuantity());
        if (request.getProductBy()            != null) generator.setProductBy(request.getProductBy());
        if (request.getDescription()          != null) generator.setDescription(request.getDescription());
        if (request.getIsActive()             != null) generator.setIsActive(request.getIsActive());

        // Handle image replacement
        if (image != null && !image.isEmpty()) {
            if (generator.getImageUrl() != null) {
                fileUploadService.deleteFile(generator.getImageUrl());
            }
            String relativePath = fileUploadService.uploadImage(image, DIR_GENERATOR_IMAGES);
            generator.setImageUrl(relativePath);
        }

        Generator saved = generatorRepository.save(generator);
        log.info("Generator updated: id={}", saved.getId());

        return toResponseWithEffectiveStock(saved);
    }

    // ── Soft-Delete ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        Generator generator = findOrThrow(id);
        generator.setDeleted(true);
        generatorRepository.save(generator);
        log.info("Generator soft-deleted: id={}", id);
    }

    // ── Availability with Date Range ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<GeneratorResponse> getForDropdownWithAvailability(LocalDate startDate, LocalDate endDate, Long excludeOrderId) {
        List<Generator> generators = generatorRepository.findAll().stream()
                .filter(g -> g.getIsActive() != null && g.getIsActive() && (g.getDeleted() == null || !g.getDeleted()))
                .collect(Collectors.toList());

        if (startDate == null || endDate == null) {
            return generators.stream()
                    .map(g -> {
                        GeneratorResponse res = toResponseWithEffectiveStock(g);
                        // Without date filter, effectiveStock already = total - underService
                        res.setAvailableStock(res.getEffectiveStock());
                        return res;
                    })
                    .collect(Collectors.toList());
        }

        List<OrderItem> overlappingBookings = generatorRepository.findAllOverlappingBookings(startDate, endDate, excludeOrderId);

        Map<Long, List<OrderItem>> bookingsByGenerator = overlappingBookings.stream()
                .filter(oi -> oi.getGenerator() != null)
                .collect(Collectors.groupingBy(oi -> oi.getGenerator().getId()));

        List<GeneratorResponse> responses = new ArrayList<>();
        for (Generator g : generators) {
            int totalStock = g.getStockQuantity() != null ? g.getStockQuantity() : 0;
            int underService = g.getUnderServiceQuantity() != null ? g.getUnderServiceQuantity() : 0;
            int effectiveTotal = Math.max(0, totalStock - underService);

            List<OrderItem> bookings = bookingsByGenerator.getOrDefault(g.getId(), Collections.emptyList());

            int minAvailable = effectiveTotal;
            for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                final LocalDate currentDay = d;
                int bookedOnDay = bookings.stream()
                        .filter(oi -> {
                            Order o = oi.getOrder();
                            return o != null
                                && o.getFunctionDateFrom() != null
                                && o.getFunctionDateTo() != null
                                && !o.getFunctionDateFrom().isAfter(currentDay)
                                && !o.getFunctionDateTo().isBefore(currentDay);
                        })
                        .mapToInt(oi -> oi.getQuantity() != null ? oi.getQuantity() : 0)
                        .sum();

                int availableOnDay = effectiveTotal - bookedOnDay;
                if (availableOnDay < minAvailable) {
                    minAvailable = availableOnDay;
                }
            }

            if (minAvailable < 0) minAvailable = 0;

            GeneratorResponse res = toResponseWithEffectiveStock(g);
            res.setAvailableStock(minAvailable);
            responses.add(res);
        }

        return responses;
    }

    // ── Daily Availability Breakdown ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDailyAvailability(Long generatorId, LocalDate startDate, LocalDate endDate) {
        Generator generator = findOrThrow(generatorId);

        int totalStock = generator.getStockQuantity() != null ? generator.getStockQuantity() : 0;
        int underService = generator.getUnderServiceQuantity() != null ? generator.getUnderServiceQuantity() : 0;
        int effectiveTotal = Math.max(0, totalStock - underService);

        // Fetch overlapping bookings for this generator across the date range
        List<OrderItem> allBookings = generatorRepository.findAllOverlappingBookings(startDate, endDate, null)
                .stream()
                .filter(oi -> oi.getGenerator() != null && oi.getGenerator().getId().equals(generatorId))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            final LocalDate currentDay = d;

            // Count booked units on this specific day
            int bookedOnDay = allBookings.stream()
                    .filter(oi -> {
                        Order o = oi.getOrder();
                        return o != null
                            && o.getFunctionDateFrom() != null
                            && o.getFunctionDateTo() != null
                            && !o.getFunctionDateFrom().isAfter(currentDay)
                            && !o.getFunctionDateTo().isBefore(currentDay);
                    })
                    .mapToInt(oi -> oi.getQuantity() != null ? oi.getQuantity() : 0)
                    .sum();

            int available = Math.max(0, effectiveTotal - bookedOnDay);

            // Collect booking details for this day
            List<Map<String, Object>> dayBookings = allBookings.stream()
                    .filter(oi -> {
                        Order o = oi.getOrder();
                        return o != null
                            && o.getFunctionDateFrom() != null
                            && o.getFunctionDateTo() != null
                            && !o.getFunctionDateFrom().isAfter(currentDay)
                            && !o.getFunctionDateTo().isBefore(currentDay);
                    })
                    .map(oi -> {
                        Map<String, Object> b = new LinkedHashMap<>();
                        b.put("orderId", oi.getOrder().getId());
                        b.put("orderNumber", oi.getOrder().getOrderNumber());
                        b.put("clientName", oi.getOrder().getCustomer() != null ? oi.getOrder().getCustomer().getName() : "—");
                        b.put("quantity", oi.getQuantity() != null ? oi.getQuantity() : 0);
                        b.put("functionDateFrom", oi.getOrder().getFunctionDateFrom());
                        b.put("functionDateTo", oi.getOrder().getFunctionDateTo());
                        return b;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> dayEntry = new LinkedHashMap<>();
            dayEntry.put("date", currentDay.toString());
            dayEntry.put("totalStock", totalStock);
            dayEntry.put("underServiceQty", underService);
            dayEntry.put("effectiveStock", effectiveTotal);
            dayEntry.put("bookedQty", bookedOnDay);
            dayEntry.put("availableQty", available);
            dayEntry.put("bookings", dayBookings);
            result.add(dayEntry);
        }

        return result;
    }

    // ── Daily Availability Breakdown For All Active Generators ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDailyAvailabilityAll(LocalDate date) {
        List<Generator> generators = generatorRepository.findAll().stream()
                .filter(g -> g.getIsActive() != null && g.getIsActive() && (g.getDeleted() == null || !g.getDeleted()))
                .collect(Collectors.toList());

        List<OrderItem> overlappingBookings = generatorRepository.findAllOverlappingBookings(date, date, null);

        Map<Long, List<OrderItem>> bookingsByGenerator = overlappingBookings.stream()
                .filter(oi -> oi.getGenerator() != null)
                .collect(Collectors.groupingBy(oi -> oi.getGenerator().getId()));

        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Generator g : generators) {
            int totalStock = g.getStockQuantity() != null ? g.getStockQuantity() : 0;
            int underService = g.getUnderServiceQuantity() != null ? g.getUnderServiceQuantity() : 0;
            int effectiveTotal = Math.max(0, totalStock - underService);

            List<OrderItem> bookings = bookingsByGenerator.getOrDefault(g.getId(), Collections.emptyList());

            int bookedOnDay = bookings.stream()
                    .filter(oi -> {
                        Order o = oi.getOrder();
                        return o != null
                            && o.getFunctionDateFrom() != null
                            && o.getFunctionDateTo() != null
                            && !o.getFunctionDateFrom().isAfter(date)
                            && !o.getFunctionDateTo().isBefore(date);
                    })
                    .mapToInt(oi -> oi.getQuantity() != null ? oi.getQuantity() : 0)
                    .sum();

            int available = Math.max(0, effectiveTotal - bookedOnDay);

            List<Map<String, Object>> dayBookings = bookings.stream()
                    .filter(oi -> {
                        Order o = oi.getOrder();
                        return o != null
                            && o.getFunctionDateFrom() != null
                            && o.getFunctionDateTo() != null
                            && !o.getFunctionDateFrom().isAfter(date)
                            && !o.getFunctionDateTo().isBefore(date);
                    })
                    .map(oi -> {
                        Map<String, Object> b = new LinkedHashMap<>();
                        b.put("orderId", oi.getOrder().getId());
                        b.put("orderNumber", oi.getOrder().getOrderNumber());
                        b.put("quantity", oi.getQuantity());
                        return b;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("generatorId", g.getId());
            entry.put("generatorName", g.getName());
            entry.put("generatorCode", g.getGeneratorCode());
            entry.put("partyDieselRentPrice", g.getPartyDieselRentPrice());
            entry.put("withDieselRentPrice", g.getWithDieselRentPrice());
            entry.put("totalStock", totalStock);
            entry.put("underServiceQty", underService);
            entry.put("effectiveStock", effectiveTotal);
            entry.put("bookedQty", bookedOnDay);
            entry.put("availableQty", available);
            entry.put("bookings", dayBookings);
            
            result.add(entry);
        }

        return result;
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Generator findOrThrow(Long id) {
        return generatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Generator not found with id: " + id));
    }

    /**
     * Maps a Generator entity to a response DTO with effectiveStock pre-computed.
     * effectiveStock = stockQuantity - underServiceQuantity (before date-based deduction)
     */
    private GeneratorResponse toResponseWithEffectiveStock(Generator g) {
        GeneratorResponse res = generatorMapper.toResponse(g);
        int total = g.getStockQuantity() != null ? g.getStockQuantity() : 0;
        int underService = g.getUnderServiceQuantity() != null ? g.getUnderServiceQuantity() : 0;
        res.setUnderServiceQuantity(underService);
        res.setEffectiveStock(Math.max(0, total - underService));
        return res;
    }
}
