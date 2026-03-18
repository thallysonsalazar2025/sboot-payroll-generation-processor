package com.example.payroll.infrastructure.amqp;

import com.example.payroll.application.PayrollProcessorService;
import com.example.payroll.application.mapper.PayrollMessageMapper;
import com.example.payroll.domain.model.PayrollItem;
import com.example.payroll.domain.model.PayrollItemType;
import com.example.payroll.domain.service.PayrollCalculator;
import com.example.payroll.infrastructure.persistence.InMemoryPayrollDocumentRepository;
import com.example.payroll.support.InMemoryPayrollItemsPort;
import com.example.payroll.support.TestSupport;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

public class PayrollGenerationListenerTest {
    public static void main(String[] args) {
        shouldConvertJsonAndProcessMessage();
        shouldRejectInvalidPayload();
        System.out.println("PayrollGenerationListenerTest OK");
    }

    static void shouldConvertJsonAndProcessMessage() {
        var repository = new InMemoryPayrollDocumentRepository();
        var itemsPort = new InMemoryPayrollItemsPort().put("company-a", "emp-1", 3, 2026, List.of(
                new PayrollItem("Base salary", PayrollItemType.CREDIT, new BigDecimal("3500.00")),
                new PayrollItem("Bonus", PayrollItemType.CREDIT, new BigDecimal("250.00")),
                new PayrollItem("INSS", PayrollItemType.DEBIT, new BigDecimal("410.00")),
                new PayrollItem("Meal voucher", PayrollItemType.DEBIT, new BigDecimal("90.00"))));
        var service = new PayrollProcessorService(
                (request, gross, discount, net) -> new com.example.payroll.domain.model.PdfDocument("pay.pdf", "pdf".getBytes()),
                (document, tenantId, employeeId) -> new com.example.payroll.domain.model.StoredFile("bucket/key/pay.pdf", "https://files/pay.pdf"),
                repository,
                new InMemoryTopicPublisher<>(),
                new InMemoryTopicPublisher<>(),
                new PayrollCalculator(itemsPort),
                new PayrollMessageMapper(),
                Clock.systemUTC());
        var listener = new PayrollGenerationListener(new PayrollRequestJsonParser(), new PayrollMessageMapper(), service);

        listener.onMessage(sampleJson());

        TestSupport.assertEquals(1, repository.size(), "listener should process and persist one document");
        var document = repository.findByPayrollPeriod("company-a", "emp-1", 3, 2026).orElseThrow();
        TestSupport.assertEquals("3750.00", document.grossAmount().toPlainString(), "gross should be persisted from payroll items");
        TestSupport.assertEquals("500.00", document.discountAmount().toPlainString(), "discount should be persisted from payroll items");
    }

    static void shouldRejectInvalidPayload() {
        var listener = new PayrollGenerationListener(new PayrollRequestJsonParser(), new PayrollMessageMapper(), null);
        TestSupport.expectThrows(IllegalArgumentException.class, () -> listener.onMessage("{invalid}"), "invalid payload should fail");
    }

    static String sampleJson() {
        return """
                {
                  \"employeeId\": \"emp-1\",
                  \"companyId\": \"company-a\",
                  \"requesterId\": \"requester-9\",
                  \"month\": 3,
                  \"year\": 2026
                }
                """;
    }
}
