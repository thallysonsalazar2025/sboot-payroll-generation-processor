package com.example.payroll.domain.service;

import com.example.payroll.domain.model.PayrollGenerationRequest;
import com.example.payroll.domain.model.PayrollItemType;
import java.math.BigDecimal;

public class PayrollCalculator {
    public BigDecimal grossAmount(PayrollGenerationRequest request) {
        return request.items().stream()
                .filter(item -> item.type() == PayrollItemType.CREDIT)
                .map(item -> item.amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal discountAmount(PayrollGenerationRequest request) {
        return request.items().stream()
                .filter(item -> item.type() == PayrollItemType.DEBIT)
                .map(item -> item.amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal netAmount(PayrollGenerationRequest request) {
        return grossAmount(request).subtract(discountAmount(request));
    }
}
