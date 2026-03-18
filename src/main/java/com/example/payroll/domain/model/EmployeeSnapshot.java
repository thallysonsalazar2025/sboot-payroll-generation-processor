package com.example.payroll.domain.model;

import java.util.Objects;

public record EmployeeSnapshot(String employeeId, String employeeName, String documentNumber, String email) {
    public EmployeeSnapshot {
        Objects.requireNonNull(employeeId);
        Objects.requireNonNull(employeeName);
        Objects.requireNonNull(documentNumber);
        Objects.requireNonNull(email);
    }
}
