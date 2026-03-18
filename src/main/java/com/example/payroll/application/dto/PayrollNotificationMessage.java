package com.example.payroll.application.dto;

import com.example.payroll.domain.model.PayrollProcessingStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PayrollNotificationMessage(UUID requestId, PayrollProcessingStatus status, String detail, OffsetDateTime notifiedAt) {
}
