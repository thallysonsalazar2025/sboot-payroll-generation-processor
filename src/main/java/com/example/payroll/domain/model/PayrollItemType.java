package com.example.payroll.domain.model;

public enum PayrollItemType {
    CREDIT,
    DEBIT;

    public static PayrollItemType from(String value) {
        return PayrollItemType.valueOf(value.trim().toUpperCase());
    }
}
