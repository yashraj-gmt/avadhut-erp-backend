package com.erp.system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Result returned by the regular-customer recalculation endpoint/job.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegularCustomerRecalcResult {

    /** Total active customers evaluated */
    private int totalProcessed;

    /** Customers newly or already flagged as regular after recalc */
    private int totalFlagged;

    /** Customers whose regular flag was removed */
    private int totalUnflagged;

    /** Config used: minimum orders threshold */
    private int minOrdersUsed;

    /** Config used: lookback window in months */
    private int lookbackMonthsUsed;

    /** Config used: minimum total spend threshold */
    private java.math.BigDecimal minTotalSpendUsed;

    private LocalDateTime recalculatedAt;
}
