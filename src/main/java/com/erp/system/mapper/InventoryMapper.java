// com/erp/system/mapper/InventoryMapper.java
package com.erp.system.mapper;

import com.erp.system.dto.response.InventoryResponse;
import com.erp.system.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(source = "product.id",          target = "productId")
    @Mapping(source = "product.name",        target = "productName")
    @Mapping(source = "product.productCode", target = "productCode")
    @Mapping(source = "warehouse.id",        target = "warehouseId")
    @Mapping(source = "warehouse.name",      target = "warehouseName")
    @Mapping(source = "warehouse.code",      target = "warehouseCode")
        // stockAlertPercentage, isLowStock, capacityUtilisationPercentage
        // are @Transient getters on the entity — MapStruct calls them automatically
    InventoryResponse toResponse(Inventory inventory);
}