package com.erp.system.service.impl;

import com.erp.system.dto.request.CreateGeneratorRequest;
import com.erp.system.dto.request.UpdateGeneratorRequest;
import com.erp.system.dto.response.GeneratorResponse;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.entity.Generator;
import com.erp.system.enums.GeneratorFuelType;
import com.erp.system.enums.GeneratorStatus;
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

        // Validate unique serial number if provided
        if (request.getSerialNumber() != null && !request.getSerialNumber().isBlank()
                && generatorRepository.existsBySerialNumberIgnoreCase(request.getSerialNumber())) {
            throw new AppException(
                    "Generator with serial number '" + request.getSerialNumber() + "' already exists.",
                    HttpStatus.CONFLICT
            );
        }

        Generator generator = new Generator();
        generator.setName(request.getName().trim());
        generator.setGeneratorCode(request.getGeneratorCode().trim());
        generator.setBrand(request.getBrand());
        generator.setModel(request.getModel());
        generator.setSerialNumber(request.getSerialNumber());
        generator.setFuelType(request.getFuelType());
        generator.setRatedPowerKva(request.getRatedPowerKva());
        generator.setRatedPowerKw(request.getRatedPowerKw());
        generator.setVoltage(request.getVoltage());
        generator.setFrequency(request.getFrequency());
        generator.setPurchaseDate(request.getPurchaseDate());
        generator.setPurchasePrice(request.getPurchasePrice());
        generator.setRentPricePerDay(request.getRentPricePerDay());
        generator.setCurrentStatus(
                request.getCurrentStatus() != null ? request.getCurrentStatus() : GeneratorStatus.AVAILABLE
        );
        generator.setCondition(request.getCondition());
        generator.setLocation(request.getLocation());
        generator.setHoursRun(request.getHoursRun() != null ? request.getHoursRun() : 0);
        generator.setLastServiceDate(request.getLastServiceDate());
        generator.setNextServiceDue(request.getNextServiceDue());
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
    public PagedResponse<GeneratorResponse> getAll(String search, GeneratorStatus currentStatus,
                                                    GeneratorFuelType fuelType, Boolean isActive,
                                                    Pageable pageable) {
        Page<Generator> page = generatorRepository.findAllWithFilters(
                search, currentStatus, fuelType, isActive, pageable
        );

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
            String newCode = request.getGeneratorCode().trim();
            if (generatorRepository.existsByGeneratorCodeIgnoreCaseAndIdNot(newCode, id)) {
                throw new AppException(
                        "Generator code '" + newCode + "' is already taken.",
                        HttpStatus.CONFLICT
                );
            }
            generator.setGeneratorCode(newCode);
        }

        // Serial number uniqueness check
        if (request.getSerialNumber() != null) {
            if (!request.getSerialNumber().isBlank()
                    && generatorRepository.existsBySerialNumberIgnoreCaseAndIdNot(request.getSerialNumber(), id)) {
                throw new AppException(
                        "Serial number '" + request.getSerialNumber() + "' is already taken.",
                        HttpStatus.CONFLICT
                );
            }
            generator.setSerialNumber(request.getSerialNumber());
        }

        if (request.getBrand()          != null) generator.setBrand(request.getBrand());
        if (request.getModel()          != null) generator.setModel(request.getModel());
        if (request.getFuelType()       != null) generator.setFuelType(request.getFuelType());
        if (request.getRatedPowerKva()  != null) generator.setRatedPowerKva(request.getRatedPowerKva());
        if (request.getRatedPowerKw()   != null) generator.setRatedPowerKw(request.getRatedPowerKw());
        if (request.getVoltage()        != null) generator.setVoltage(request.getVoltage());
        if (request.getFrequency()      != null) generator.setFrequency(request.getFrequency());
        if (request.getPurchaseDate()   != null) generator.setPurchaseDate(request.getPurchaseDate());
        if (request.getPurchasePrice()  != null) generator.setPurchasePrice(request.getPurchasePrice());
        if (request.getRentPricePerDay()!= null) generator.setRentPricePerDay(request.getRentPricePerDay());
        if (request.getCurrentStatus()  != null) generator.setCurrentStatus(request.getCurrentStatus());
        if (request.getCondition()      != null) generator.setCondition(request.getCondition());
        if (request.getLocation()       != null) generator.setLocation(request.getLocation());
        if (request.getHoursRun()       != null) generator.setHoursRun(request.getHoursRun());
        if (request.getLastServiceDate()!= null) generator.setLastServiceDate(request.getLastServiceDate());
        if (request.getNextServiceDue() != null) generator.setNextServiceDue(request.getNextServiceDue());
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
