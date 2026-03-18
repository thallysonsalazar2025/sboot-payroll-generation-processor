package com.example.payroll.infrastructure.amqp;

import com.example.payroll.application.PayrollProcessorService;
import com.example.payroll.application.mapper.PayrollMessageMapper;
import com.example.payroll.domain.service.PayrollCalculator;
import com.example.payroll.infrastructure.persistence.InMemoryPayrollDocumentRepository;
import com.example.payroll.support.TestSupport;
import java.time.Clock;

public class PayrollGenerationListenerTest {
    public static void main(String[] args) {
        shouldConvertJsonAndProcessMessage();
        shouldRejectInvalidPayload();
        System.out.println("PayrollGenerationListenerTest OK");
    }

    static void shouldConvertJsonAndProcessMessage() {
        var repository = new InMemoryPayrollDocumentRepository();
        var service = new PayrollProcessorService(
                (request, gross, discount, net) -> new com.example.payroll.domain.model.PdfDocument("pay.pdf", "pdf".getBytes()),
                (document, tenantId, employeeId) -> new com.example.payroll.domain.model.StoredFile("bucket/key/pay.pdf", "https://files/pay.pdf"),
                repository,
                new InMemoryTopicPublisher<>(),
                new InMemoryTopicPublisher<>(),
                new PayrollCalculator(),
                new PayrollMessageMapper(),
                Clock.systemUTC());
        var listener = new PayrollGenerationListener(new PayrollRequestJsonParser(), new PayrollMessageMapper(), service);

        listener.onMessage(sampleJson());

        TestSupport.assertEquals(1, repository.size(), "listener should process and persist one document");
        TestSupport.assertTrue(repository.findByPayrollPeriod("company-a", "emp-1", 3, 2026).isPresent(), "document should be indexed by period");
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
