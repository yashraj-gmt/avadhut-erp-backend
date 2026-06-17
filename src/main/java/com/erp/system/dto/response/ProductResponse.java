package com.erp.system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {

    private Long    id;
    private String  name;
    private String  productCode;

    private BigDecimal purchasePrice;
    private Integer    currentStock;
    private String     description;
    private String     productBy;
    private Boolean    isActive;

    /** Gallery images (ordered by displayOrder ASC) */
    private List<ProductImageResponse> images;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}