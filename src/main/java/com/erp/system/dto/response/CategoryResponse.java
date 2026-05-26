package com.erp.system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {

    private Long      id;
    private String    name;
    private String    description;
    private Boolean   isActive;
    private Long      productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}