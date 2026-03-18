package com.example.payroll.domain.model;

import java.util.Objects;

public record PayrollGenerationRequest(
        String employeeId,
        String companyId,
        String requesterId,
        Integer month,
        Integer year
) {
    public PayrollGenerationRequest {
        Objects.requireNonNull(employeeId);
        Objects.requireNonNull(companyId);
        Objects.requireNonNull(requesterId);
        Objects.requireNonNull(month);
        Objects.requireNonNull(year);
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        if (year < 1900) {
            throw new IllegalArgumentException("year must be greater than or equal to 1900");
        }
    }
}
