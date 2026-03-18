package com.example.payroll.infrastructure.pdf;

import com.example.payroll.domain.model.PayrollGenerationRequest;
import com.example.payroll.domain.model.PdfDocument;
import com.example.payroll.domain.port.PdfGeneratorPort;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class SimplePdfGeneratorAdapter implements PdfGeneratorPort {
    @Override
    public PdfDocument generate(PayrollGenerationRequest request, BigDecimal grossAmount, BigDecimal discountAmount, BigDecimal netAmount) {
        String period = "%04d-%02d".formatted(request.year(), request.month());
        String uniqueSuffix = UUID.randomUUID().toString();
        String body = "PAYSLIP|companyId=%s|employeeId=%s|requesterId=%s|period=%s|gross=%s|discounts=%s|net=%s".formatted(
                request.companyId(), request.employeeId(), request.requesterId(), period, grossAmount, discountAmount, netAmount);
        return new PdfDocument("payslip-%s-%s-%s.pdf".formatted(request.employeeId(), period, uniqueSuffix), body.getBytes(StandardCharsets.UTF_8));
    }
}
