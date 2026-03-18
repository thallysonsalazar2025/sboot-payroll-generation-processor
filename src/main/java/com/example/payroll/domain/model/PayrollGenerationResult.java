package com.example.payroll.domain.model;

import java.time.OffsetDateTime;

public record PayrollGenerationResult(
        String companyId,
        String employeeId,
        String requesterId,
        Integer month,
        Integer year,
        PayrollProcessingStatus status,
        String fileUrl,
        String message,
        OffsetDateTime processedAt
) {
}
