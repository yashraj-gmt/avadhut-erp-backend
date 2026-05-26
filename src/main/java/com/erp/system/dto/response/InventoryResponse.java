package com.erp.system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryResponse {

    private Long    id;

    // Product summary
    private Long    productId;
    private String  productName;
    private String  productCode;

    // Warehouse summary
    private Long    warehouseId;
    private String  warehouseName;
    private String  warehouseCode;

    // Stock data
    private Integer stockQuantity;
    private Integer stockAlert;
    private Integer warehouseCapacity;

    /**
     * Alert percentage = (stockQuantity / stockAlert) * 100
     *  < 100 → LOW STOCK (stock is below alert threshold)
     * >= 100 → OK      (stock is at or above alert threshold)
     */
    private Double  stockAlertPercentage;

    private Boolean isLowStock;

    /** Capacity utilisation = (stockQuantity / warehouseCapacity) * 100 */
    private Double  capacityUtilisationPercentage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}