package com.erp.system.service;

import com.erp.system.dto.request.CreateGeneratorRequest;
import com.erp.system.dto.request.UpdateGeneratorRequest;
import com.erp.system.dto.response.GeneratorResponse;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.enums.GeneratorFuelType;
import com.erp.system.enums.GeneratorStatus;
import org.springframework.data.domain.Pageable;

public interface GeneratorService {

    GeneratorResponse create(CreateGeneratorRequest request);

    PagedResponse<GeneratorResponse> getAll(String search, GeneratorStatus currentStatus,
                                            GeneratorFuelType fuelType, Boolean isActive,
                                            Pageable pageable);

    GeneratorResponse getById(Long id);

    GeneratorResponse update(Long id, UpdateGeneratorRequest request);

    void delete(Long id);
}
