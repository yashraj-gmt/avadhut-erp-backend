package com.erp.system.dto.request;

import com.erp.system.enums.GeneratorCondition;
import com.erp.system.enums.GeneratorFuelType;
import com.erp.system.enums.GeneratorStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateGeneratorRequest {

    @NotBlank(message = "Generator name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Generator code is required")
    @Size(max = 50, message = "Generator code must not exceed 50 characters")
    private String generatorCode;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @Size(max = 100, message = "Serial number must not exceed 100 characters")
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

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    private Integer hoursRun;

    private LocalDate lastServiceDate;

    private LocalDate nextServiceDue;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Boolean isActive = true;
}
