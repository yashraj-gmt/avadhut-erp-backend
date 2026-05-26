package com.erp.system.dto.response;

import com.erp.system.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {

    private Long    id;
    private String  name;
    private String  productCode;

    // Category
    private Long    categoryId;
    private String  categoryName;

    private String     unit;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private BigDecimal rentPrice;
    private Integer    currentStock;
    private Integer    minimumStock;
    private String     hsnCode;
    private BigDecimal gstPercent;
    private String     description;
    private Boolean    isActive;

    /** DRAFT or PUBLISHED */
    private ProductStatus status;

    /** Full accessible URL for the QR code image */
    private String qrCodeUrl;

    /** Gallery images (ordered by displayOrder ASC) */
    private List<ProductImageResponse> images;

    /** Inventory details across warehouses */
    private List<InventoryResponse> inventories;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}