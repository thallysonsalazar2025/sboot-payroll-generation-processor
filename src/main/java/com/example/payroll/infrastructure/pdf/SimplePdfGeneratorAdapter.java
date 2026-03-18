package com.example.payroll.infrastructure.pdf;

import com.example.payroll.domain.model.PayrollGenerationRequest;
import com.example.payroll.domain.model.PdfDocument;
import com.example.payroll.domain.port.PdfGeneratorPort;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class SimplePdfGeneratorAdapter implements PdfGeneratorPort {
    @Override
    public PdfDocument generate(PayrollGenerationRequest request, BigDecimal grossAmount, BigDecimal discountAmount, BigDecimal netAmount) {
        String body = "PAYSLIP|requestId=%s|employee=%s|gross=%s|discounts=%s|net=%s".formatted(
                request.requestId(), request.employee().employeeName(), grossAmount, discountAmount, netAmount);
        return new PdfDocument("payslip-" + request.requestId() + ".pdf", body.getBytes(StandardCharsets.UTF_8));
    }
}
