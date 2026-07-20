package com.erp.system.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    
    private String clientName;
    private String contactNumber;
    private String alternateMobile;
    
    private String operatorName;
    private String operatorMobile;
    
    private Boolean cableRequired;
    private String dieselType;
    
    private String siteAddress;
    private String siteAddressLink;
    private String remarks;
    
    private LocalDate functionDateFrom;
    private LocalDate functionDateTo;
    private String functionDate; // computed "YYYY-MM-DD to YYYY-MM-DD"
    
    private String orderStatus;
    private String billingStatus;
    
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal finalAmount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<OrderItemResponse> generators;

    @Data
    @Builder
    public static class OrderItemResponse {
        private Long id;
        private Long generatorId;
        private String generatorName;
        private String generatorCode; // Added code if needed
        private String cableSize;
        
        private LocalTime startTime;
        private LocalTime endTime;
        private BigDecimal duration;
        
        private BigDecimal rate;
        private BigDecimal dieselRate;
        private BigDecimal totalAmount;

        private List<DieselEntryResponse> dieselEntries;
    }

    @Data
    @Builder
    public static class DieselEntryResponse {
        private Long id;
        private LocalDate entryDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private BigDecimal duration;
    }
}
