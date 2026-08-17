package com.erp.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotBlank
    private String clientName;
    
    @NotBlank
    private String contactNumber;
    
    private String alternateMobile;
    
    @NotBlank
    private String operatorName;
    
    private String operatorMobile;
    
    @NotNull
    private Boolean cableRequired;
    
    private String dieselType;
    
    @NotBlank
    private String siteAddress;
    
    private String siteAddressLink;
    
    private String remarks;
    
    private LocalDate functionDateFrom;
    private LocalDate functionDateTo;

    // Optional: map the single string "YYYY-MM-DD to YYYY-MM-DD"
    private String functionDate;

    private List<OrderItemRequest> generators;

    @Data
    public static class OrderItemRequest {
        // Accepts either a numeric ID (Long as string) or a generator code like "GEN-012"
        @NotBlank
        private String generatorId;
        
        private String cableSize;
        
        private java.math.BigDecimal cableRate;
        
        private LocalTime startTime;
        
        private LocalTime endTime;
        
        private java.math.BigDecimal duration;
        
        private java.math.BigDecimal rate; // in case frontend overrides the default rate
    }
}
