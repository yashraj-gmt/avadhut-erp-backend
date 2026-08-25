package com.erp.system.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class UpdateOrderBillingRequest {

    private BigDecimal discountAmount;

    /** Optional due date for payment. If null, backend defaults to today + 7 days. */
    private LocalDate paymentDueDate;

    @NotNull
    private List<BillingItemRequest> generators;

    /** Optional miscellaneous/other charges (e.g. catering, extra services). */
    private List<OtherChargeRequest> otherCharges;

    @Data
    public static class BillingItemRequest {
        @NotNull
        private Long orderItemId;

        @NotNull
        private BigDecimal rentPerDay;

        @NotNull
        private BigDecimal dieselPerHour;

        private BigDecimal cableRate;

        private List<DieselEntryRequest> dieselEntries;
    }

    @Data
    public static class DieselEntryRequest {
        @NotNull
        private LocalDate entryDate;

        @NotNull
        private LocalTime startTime;

        @NotNull
        private LocalTime endTime;

        @NotNull
        private BigDecimal duration;
    }

    @Data
    public static class OtherChargeRequest {
        /** Display label for this charge (e.g. "Catering", "Maintenance"). */
        private String name;

        /** Amount for this charge. */
        private BigDecimal amount;
    }
}
