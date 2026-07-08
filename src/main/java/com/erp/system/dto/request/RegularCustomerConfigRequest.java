package com.erp.system.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Body accepted by the on-demand regular-customer recalculation endpoint.
 * Values here override the defaults from application.properties.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegularCustomerConfigRequest {

    /** Minimum number of orders in the lookback window to qualify as regular. */
    @Min(value = 1, message = "minOrders must be at least 1.")
    private Integer minOrders;

    /** Lookback window in months (how far back to count orders). */
    @Min(value = 1, message = "lookbackMonths must be at least 1.")
    @Max(value = 120, message = "lookbackMonths must not exceed 120.")
    private Integer lookbackMonths;

    /**
     * Minimum cumulative total spend (sum of finalAmount across all orders) to qualify.
     * Either minOrders OR minTotalSpend threshold can qualify a customer (OR logic).
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "minTotalSpend must be positive.")
    private BigDecimal minTotalSpend;
}
