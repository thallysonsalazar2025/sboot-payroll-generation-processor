package com.example.payroll.interfaces.rest;

import com.example.payroll.domain.model.PayrollDocument;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PayrollDocumentResponse(
        UUID requestId,
        String tenantId,
        String employeeId,
        String fileName,
        String fileUrl,
        BigDecimal grossAmount,
        BigDecimal discountAmount,
        BigDecimal netAmount,
        String status,
        OffsetDateTime processedAt
) {
    public static PayrollDocumentResponse from(PayrollDocument document) {
        return new PayrollDocumentResponse(document.requestId(), document.tenantId(), document.employeeId(), document.fileName(), document.fileUrl(), document.grossAmount(), document.discountAmount(), document.netAmount(), document.status().name(), document.processedAt());
    }
}
