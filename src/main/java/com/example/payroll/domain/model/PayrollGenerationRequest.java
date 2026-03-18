package com.example.payroll.domain.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record PayrollGenerationRequest(
        UUID requestId,
        String tenantId,
        EmployeeSnapshot employee,
        LocalDate payrollDate,
        String currency,
        List<PayrollItem> items,
        OffsetDateTime requestedAt,
        String callbackTopic
) {
    public PayrollGenerationRequest {
        Objects.requireNonNull(requestId);
        Objects.requireNonNull(tenantId);
        Objects.requireNonNull(employee);
        Objects.requireNonNull(payrollDate);
        Objects.requireNonNull(currency);
        Objects.requireNonNull(items);
        Objects.requireNonNull(requestedAt);
        Objects.requireNonNull(callbackTopic);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
    }
}
