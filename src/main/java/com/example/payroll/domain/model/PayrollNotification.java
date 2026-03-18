package com.example.payroll.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PayrollNotification(UUID requestId, PayrollProcessingStatus status, String detail, OffsetDateTime notifiedAt) {
}
