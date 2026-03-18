package com.example.payroll.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PayrollGenerationResult(
        UUID requestId,
        String tenantId,
        String employeeId,
        PayrollProcessingStatus status,
        String fileUrl,
        String message,
        OffsetDateTime processedAt
) {
}
