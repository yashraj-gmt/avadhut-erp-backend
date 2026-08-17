package com.erp.system.mapper;

import com.erp.system.dto.response.GeneratorResponse;
import com.erp.system.entity.Generator;
import com.erp.system.service.FileUploadService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Maps Generator entity → GeneratorResponse DTO.
 *
 * Uses an abstract class so Spring can inject FileUploadService.
 * MapStruct generates GeneratorMapperImpl as a Spring @Component.
 *
 * Root-cause note: @AfterMapping with @MappingTarget does NOT work when the
 * DTO uses @Builder, because MapStruct calls builder().build() and returns the
 * immutable object before the @AfterMapping hook can mutate it via a setter.
 * Solution: map imageUrl directly via a @Mapping expression so it is set
 * inside the builder chain, before build() is called.
 */
@Mapper(componentModel = "spring")
public abstract class GeneratorMapper {

    @Autowired
    protected FileUploadService fileUploadService;

    /**
     * Maps all Generator fields to GeneratorResponse.
     * imageUrl is converted from a relative DB path to a public URL inline.
     */
    @Mapping(
        target = "imageUrl",
        expression = "java(fileUploadService.toPublicUrl(generator.getImageUrl()))"
    )
    public abstract GeneratorResponse toResponse(Generator generator);
}
