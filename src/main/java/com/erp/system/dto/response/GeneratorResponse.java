package com.erp.system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneratorResponse {

    private Long       id;
    private String     name;
    private String     generatorCode;
    private BigDecimal purchasePrice;
    private BigDecimal partyDieselRentPrice;
    private BigDecimal withDieselRentPrice;
    private Integer    stockQuantity;
    private Integer    availableStock;
    /** Units currently under service / not working (excluded from bookable stock). */
    private Integer    underServiceQuantity;
    /** Effective bookable stock = stockQuantity - underServiceQuantity (before date-based deduction). */
    private Integer    effectiveStock;
    private String     productBy;
    private String     imageUrl;
    private String     description;
    private Boolean    isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
