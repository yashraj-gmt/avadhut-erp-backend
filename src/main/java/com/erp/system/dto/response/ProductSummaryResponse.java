package com.erp.system.dto.response;

import com.erp.system.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Lightweight DTO returned in paginated product lists */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductSummaryResponse {

    private Long          id;
    private String        name;
    private String        productCode;
    private String        categoryName;
    private String        unit;
    private BigDecimal    sellingPrice;
    private Integer       currentStock;
    private ProductStatus status;
    private Boolean       isActive;
    private String        productBy;

    private Integer minimumStock;
    /** Primary image URL (null if no images uploaded) */
    private String        primaryImageUrl;

    private LocalDateTime createdAt;
}