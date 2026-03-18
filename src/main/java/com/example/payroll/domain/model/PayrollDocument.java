package com.example.payroll.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PayrollDocument(
        UUID requestId,
        String tenantId,
        String employeeId,
        String fileName,
        String storageKey,
        String fileUrl,
        BigDecimal grossAmount,
        BigDecimal discountAmount,
        BigDecimal netAmount,
        PayrollProcessingStatus status,
        OffsetDateTime processedAt
) {
}
