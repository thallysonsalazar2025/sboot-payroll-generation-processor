package com.example.payroll.interfaces.rest;

import com.example.payroll.domain.model.PayrollDocument;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PayrollDocumentResponse(
        String companyId,
        String employeeId,
        String requesterId,
        Integer month,
        Integer year,
        String fileName,
        String fileUrl,
        BigDecimal grossAmount,
        BigDecimal discountAmount,
        BigDecimal netAmount,
        String status,
        OffsetDateTime processedAt
) {
    public static PayrollDocumentResponse from(PayrollDocument document) {
        return new PayrollDocumentResponse(
                document.companyId(),
                document.employeeId(),
                document.requesterId(),
                document.month(),
                document.year(),
                document.fileName(),
                document.fileUrl(),
                document.grossAmount(),
                document.discountAmount(),
                document.netAmount(),
                document.status().name(),
                document.processedAt());
    }
}
