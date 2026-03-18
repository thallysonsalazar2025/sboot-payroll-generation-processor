package com.example.payroll.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PayrollDocument(
        String companyId,
        String employeeId,
        String requesterId,
        Integer month,
        Integer year,
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
