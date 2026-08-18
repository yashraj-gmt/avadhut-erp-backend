package com.erp.system.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private Boolean isRead;
    private Long referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
}
