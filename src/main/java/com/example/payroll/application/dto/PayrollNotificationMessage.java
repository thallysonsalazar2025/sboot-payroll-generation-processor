package com.example.payroll.application.dto;

import com.example.payroll.domain.model.PayrollProcessingStatus;
import java.time.OffsetDateTime;

public record PayrollNotificationMessage(
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
