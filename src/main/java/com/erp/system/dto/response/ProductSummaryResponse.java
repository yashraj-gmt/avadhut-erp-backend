package com.erp.system.dto.response;

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

    private Long       id;
    private String     name;
    private String     productCode;
    private BigDecimal purchasePrice;
    private BigDecimal rentPrice;
    private Integer    currentStock;
    private String     productBy;
    private Boolean    isActive;

    /** Primary image URL (null if no images uploaded) */
    private String     primaryImageUrl;

    private LocalDateTime createdAt;
}