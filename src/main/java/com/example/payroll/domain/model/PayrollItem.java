package com.example.payroll.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record PayrollItem(String description, PayrollItemType type, BigDecimal amount) {
    public PayrollItem {
        Objects.requireNonNull(description);
        Objects.requireNonNull(type);
        Objects.requireNonNull(amount);
    }
}
