package com.erp.system.mapper;

import com.erp.system.dto.response.GeneratorResponse;
import com.erp.system.entity.Generator;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GeneratorMapper {

    GeneratorResponse toResponse(Generator generator);
}
