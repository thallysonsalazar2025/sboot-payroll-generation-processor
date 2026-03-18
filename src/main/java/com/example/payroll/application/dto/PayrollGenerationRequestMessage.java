package com.example.payroll.application.dto;

public record PayrollGenerationRequestMessage(
        String employeeId,
        String companyId,
        String requesterId,
        Integer month,
        Integer year
) {
}
