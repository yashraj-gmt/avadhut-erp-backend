// com/erp/system/mapper/CategoryMapper.java
package com.erp.system.mapper;

import com.erp.system.dto.response.CategoryResponse;
import com.erp.system.entity.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    /** productCount is computed by the service and set separately. */
    @Mapping(target = "productCount", ignore = true)
    CategoryResponse toResponse(ProductCategory category);
}