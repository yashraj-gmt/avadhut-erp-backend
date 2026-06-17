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
import com.erp.system.service.GeneratorService;
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
public class GeneratorServiceImpl implements GeneratorService {

    private final GeneratorRepository generatorRepository;
    private final GeneratorMapper     generatorMapper;

    // ── Create ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public GeneratorResponse create(CreateGeneratorRequest request) {
        // Validate unique name
        if (generatorRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(
                    "Generator with name '" + request.getName() + "' already exists.",
                    HttpStatus.CONFLICT
            );
        }

        // Validate unique code
        if (generatorRepository.existsByGeneratorCodeIgnoreCase(request.getGeneratorCode())) {
            throw new AppException(
                    "Generator with code '" + request.getGeneratorCode() + "' already exists.",
                    HttpStatus.CONFLICT
            );
        }

        Generator generator = new Generator();
        generator.setName(request.getName().trim());
        generator.setGeneratorCode(request.getGeneratorCode().trim().toUpperCase());
        generator.setPurchasePrice(request.getPurchasePrice());
        generator.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        generator.setProductBy(request.getProductBy());
        generator.setImageUrl(request.getImageUrl());
        generator.setDescription(request.getDescription());
        generator.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        Generator saved = generatorRepository.save(generator);
        log.info("Generator created: id={}, name={}, code={}",
                saved.getId(), saved.getName(), saved.getGeneratorCode());

        return generatorMapper.toResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<GeneratorResponse> getAll(String search, Boolean isActive, Pageable pageable) {
        Page<Generator> page = generatorRepository.findAllWithFilters(search, isActive, pageable);
        return PagedResponse.from(page.map(generatorMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public GeneratorResponse getById(Long id) {
        Generator generator = findOrThrow(id);
        return generatorMapper.toResponse(generator);
    }

    // ── Update ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public GeneratorResponse update(Long id, UpdateGeneratorRequest request) {
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
        if (request.getGeneratorCode() != null) {
            String newCode = request.getGeneratorCode().trim().toUpperCase();
            if (generatorRepository.existsByGeneratorCodeIgnoreCaseAndIdNot(newCode, id)) {
                throw new AppException(
                        "Generator code '" + newCode + "' is already taken.",
                        HttpStatus.CONFLICT
                );
            }
            generator.setGeneratorCode(newCode);
        }

        if (request.getPurchasePrice()  != null) generator.setPurchasePrice(request.getPurchasePrice());
        if (request.getStockQuantity()  != null) generator.setStockQuantity(request.getStockQuantity());
        if (request.getProductBy()      != null) generator.setProductBy(request.getProductBy());
        if (request.getImageUrl()       != null) generator.setImageUrl(request.getImageUrl());
        if (request.getDescription()    != null) generator.setDescription(request.getDescription());
        if (request.getIsActive()       != null) generator.setIsActive(request.getIsActive());

        Generator saved = generatorRepository.save(generator);
        log.info("Generator updated: id={}", saved.getId());

        return generatorMapper.toResponse(saved);
    }

    // ── Soft-Delete ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        Generator generator = findOrThrow(id);
        generator.setDeleted(true);
        generatorRepository.save(generator);
        log.info("Generator soft-deleted: id={}", id);
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Generator findOrThrow(Long id) {
        return generatorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Generator not found with id: " + id));
    }
}
