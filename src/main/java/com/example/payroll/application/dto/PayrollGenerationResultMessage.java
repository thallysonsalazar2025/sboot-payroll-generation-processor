package com.example.payroll.application.dto;

import com.example.payroll.domain.model.PayrollProcessingStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PayrollGenerationResultMessage(
        UUID requestId,
        String tenantId,
        String employeeId,
        PayrollProcessingStatus status,
        String fileUrl,
        String message,
        OffsetDateTime processedAt
) {
}
