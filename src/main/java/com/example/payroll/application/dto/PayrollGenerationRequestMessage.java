package com.example.payroll.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PayrollGenerationRequestMessage(
        UUID requestId,
        String tenantId,
        EmployeeMessage employee,
        LocalDate payrollDate,
        String currency,
        List<PayrollItemMessage> items,
        OffsetDateTime requestedAt,
        String callbackTopic
) {
    public record EmployeeMessage(String employeeId, String employeeName, String documentNumber, String email) {}
    public record PayrollItemMessage(String description, String type, BigDecimal amount) {}
}
