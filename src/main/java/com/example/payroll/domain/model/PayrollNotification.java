package com.example.payroll.domain.model;

import java.time.OffsetDateTime;

public record PayrollNotification(
        String companyId,
        String employeeId,
        String requesterId,
        Integer month,
        Integer year,
        PayrollProcessingStatus status,
        String detail,
        OffsetDateTime notifiedAt
) {
}
