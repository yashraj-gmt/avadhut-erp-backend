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
public class WarehouseResponse {

    private Long    id;
    private String  name;
    private String  code;
    private String  location;
    private String  address;
    private String  city;
    private String  state;
    private String  pincode;
    private String  contactPerson;
    private String  contactPhone;
    private Integer totalCapacity;
    private Boolean isActive;

    /** Number of distinct product-inventory records in this warehouse */
    private Long    totalProducts;

    /** Sum of all stockQuantity across inventories in this warehouse */
    private Long    totalStockQuantity;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}