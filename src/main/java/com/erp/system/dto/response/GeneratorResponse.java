package com.erp.system.dto.response;

import com.erp.system.enums.GeneratorCondition;
import com.erp.system.enums.GeneratorFuelType;
import com.erp.system.enums.GeneratorStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeneratorResponse {

    private Long id;
    private String name;
    private String generatorCode;
    private String brand;
    private String model;
    private String serialNumber;
    private GeneratorFuelType fuelType;
    private BigDecimal ratedPowerKva;
    private BigDecimal ratedPowerKw;
    private BigDecimal voltage;
    private BigDecimal frequency;
    private LocalDate purchaseDate;
    private BigDecimal purchasePrice;
    private BigDecimal rentPricePerDay;
    private GeneratorStatus currentStatus;
    private GeneratorCondition condition;
    private String location;
    private Integer hoursRun;
    private LocalDate lastServiceDate;
    private LocalDate nextServiceDue;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
