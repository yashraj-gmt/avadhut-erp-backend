package com.erp.system.dto.response;

import com.erp.system.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Compact order summary embedded inside CustomerProfileResponse.
 * Named OrderSummaryDto to avoid collision with any future OrderSummary entity.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderSummaryDto {

    private Long        id;
    private String      orderNumber;
    private OrderStatus orderStatus;
    private BigDecimal  finalAmount;
    private LocalDate   deliveryDate;
    private LocalDateTime createdAt;
}
