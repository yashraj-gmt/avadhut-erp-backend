package com.erp.system.mapper;

import com.erp.system.dto.response.GeneratorResponse;
import com.erp.system.entity.Generator;
import com.erp.system.service.FileUploadService;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract class so Spring can inject FileUploadService for URL conversion.
 * MapStruct generates a concrete subclass and registers it as a Spring bean.
 */
@Mapper(componentModel = "spring")
public abstract class GeneratorMapper {

    @Autowired
    protected FileUploadService fileUploadService;

    /** Maps all fields except imageUrl (handled in @AfterMapping). */
    @Mapping(target = "imageUrl", ignore = true)
    public abstract GeneratorResponse toResponse(Generator generator);

    @AfterMapping
    protected void enrichResponse(Generator g, @MappingTarget GeneratorResponse r) {
        // Convert relative stored path → public HTTP URL (null-safe)
        r.setImageUrl(fileUploadService.toPublicUrl(g.getImageUrl()));
    }
}
