package com.erp.system.dto.response;

import com.erp.system.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lightweight order DTO returned to STAFF users.
 * Contains only the fields a staff member needs to see their assigned orders.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderSummaryForStaffDto {

    private Long        id;
    private String      orderNumber;

    // Customer info
    private String      customerName;
    private String      customerMobile;

    // Order details
    private OrderStatus orderStatus;
    private LocalDate   deliveryDate;
    private BigDecimal  finalAmount;
    private String      notes;

    // Assignment info
    private String      assignedByName;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
