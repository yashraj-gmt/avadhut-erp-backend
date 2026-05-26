// com/erp/system/mapper/WarehouseMapper.java
package com.erp.system.mapper;

import com.erp.system.dto.response.WarehouseResponse;
import com.erp.system.entity.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    /** Aggregate stats (totalProducts, totalStockQuantity) are set by the service. */
    @Mapping(target = "totalProducts",      ignore = true)
    @Mapping(target = "totalStockQuantity", ignore = true)
    WarehouseResponse toResponse(Warehouse warehouse);
}