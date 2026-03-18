package com.example.payroll.domain.service;

import com.example.payroll.domain.model.PayrollGenerationRequest;
import java.math.BigDecimal;

public class PayrollCalculator {
    public BigDecimal grossAmount(PayrollGenerationRequest request) {
        return BigDecimal.ZERO;
    }

    public BigDecimal discountAmount(PayrollGenerationRequest request) {
        return BigDecimal.ZERO;
    }

    public BigDecimal netAmount(PayrollGenerationRequest request) {
        return grossAmount(request).subtract(discountAmount(request));
    }
}
