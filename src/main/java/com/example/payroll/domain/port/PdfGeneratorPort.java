package com.example.payroll.domain.port;

import com.example.payroll.domain.model.PayrollGenerationRequest;
import com.example.payroll.domain.model.PdfDocument;
import java.math.BigDecimal;

public interface PdfGeneratorPort {
    PdfDocument generate(PayrollGenerationRequest request, BigDecimal grossAmount, BigDecimal discountAmount, BigDecimal netAmount);
}
