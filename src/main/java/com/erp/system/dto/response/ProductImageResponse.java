package com.erp.system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductImageResponse {

    private Long    id;
    private String  imageUrl;
    private String  originalFileName;
    private Long    fileSize;
    private Boolean isPrimary;
    private Integer displayOrder;
}