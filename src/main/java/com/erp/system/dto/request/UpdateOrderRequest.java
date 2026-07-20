package com.erp.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class UpdateOrderRequest {
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
    
    private String functionDate;

    private List<CreateOrderRequest.OrderItemRequest> generators;
}
