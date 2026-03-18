package com.example.payroll.domain.service;

import com.example.payroll.domain.model.PayrollGenerationRequest;
import com.example.payroll.domain.model.PayrollItem;
import com.example.payroll.domain.model.PayrollItemType;
import com.example.payroll.domain.port.PayrollItemsPort;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class PayrollCalculator {
    private final PayrollItemsPort payrollItemsPort;

    public PayrollCalculator() {
        this((companyId, employeeId, month, year) -> List.of());
    }

    public PayrollCalculator(PayrollItemsPort payrollItemsPort) {
        this.payrollItemsPort = Objects.requireNonNull(payrollItemsPort);
    }

    public BigDecimal grossAmount(PayrollGenerationRequest request) {
        return items(request).stream()
                .filter(item -> item.type() == PayrollItemType.CREDIT)
                .map(PayrollItem::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal discountAmount(PayrollGenerationRequest request) {
        return items(request).stream()
                .filter(item -> item.type() == PayrollItemType.DEBIT)
                .map(PayrollItem::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal netAmount(PayrollGenerationRequest request) {
        return grossAmount(request).subtract(discountAmount(request));
    }

    private List<PayrollItem> items(PayrollGenerationRequest request) {
        List<PayrollItem> items = payrollItemsPort.findByPayrollPeriod(
                request.companyId(), request.employeeId(), request.month(), request.year());
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("No payroll items found for company=%s, employee=%s, month=%d, year=%d"
                    .formatted(request.companyId(), request.employeeId(), request.month(), request.year()));
        }
        return items;
    }
}
