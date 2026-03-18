package com.example.payroll.integration;

import com.example.payroll.application.PayrollProcessorService;
import com.example.payroll.application.PayrollTopology;
import com.example.payroll.application.dto.PayrollGenerationResultMessage;
import com.example.payroll.application.dto.PayrollNotificationMessage;
import com.example.payroll.application.mapper.PayrollMessageMapper;
import com.example.payroll.domain.service.PayrollCalculator;
import com.example.payroll.infrastructure.amqp.InMemoryTopicPublisher;
import com.example.payroll.infrastructure.amqp.PayrollGenerationListener;
import com.example.payroll.infrastructure.amqp.PayrollRequestJsonParser;
import com.example.payroll.infrastructure.persistence.InMemoryPayrollDocumentRepository;
import com.example.payroll.infrastructure.pdf.SimplePdfGeneratorAdapter;
import com.example.payroll.infrastructure.storage.S3PayrollFileStorageAdapter;
import com.example.payroll.support.TestSupport;
import java.time.Clock;
import java.util.UUID;

public class PayrollProcessingFlowIntegrationTest {
    public static void main(String[] args) {
        shouldProcessTheFullFlowFromJsonToRepositoryAndTopics();
        System.out.println("PayrollProcessingFlowIntegrationTest OK");
    }

    static void shouldProcessTheFullFlowFromJsonToRepositoryAndTopics() {
        var repository = new InMemoryPayrollDocumentRepository();
        var resultPublisher = new InMemoryTopicPublisher<PayrollGenerationResultMessage>();
        var notificationPublisher = new InMemoryTopicPublisher<PayrollNotificationMessage>();
        var service = new PayrollProcessorService(
                new SimplePdfGeneratorAdapter(),
                new S3PayrollFileStorageAdapter("payroll-generated-files", "https://s3.amazonaws.com"),
                repository,
                resultPublisher,
                notificationPublisher,
                new PayrollCalculator(),
                new PayrollMessageMapper(),
                Clock.systemUTC());
        var listener = new PayrollGenerationListener(new PayrollRequestJsonParser(), new PayrollMessageMapper(), service);

        listener.onMessage(sampleJson());

        var requestId = UUID.fromString("13ef16ef-eab7-41cd-ac6d-e788a237cbec");
        var document = repository.findByRequestId(requestId).orElseThrow();
        TestSupport.assertEquals("tenant-a", document.tenantId(), "tenant should match");
        TestSupport.assertEquals(PayrollTopology.EXG_NAME_PAYROLL_GENERATION, resultPublisher.messages().getFirst().exchange(), "result exchange");
        TestSupport.assertEquals(PayrollTopology.NOTIFICATION_TOPIC, notificationPublisher.messages().getFirst().topic(), "notification topic");
        TestSupport.assertTrue(document.fileUrl().contains("https://s3.amazonaws.com/payroll-generated-files"), "stored url should be s3 based");
    }

    static String sampleJson() {
        return """
                {
                  \"requestId\": \"13ef16ef-eab7-41cd-ac6d-e788a237cbec\",
                  \"tenantId\": \"tenant-a\",
                  \"employee\": {
                    \"employeeId\": \"emp-1\",
                    \"employeeName\": \"Ana Silva\",
                    \"documentNumber\": \"12345678900\",
                    \"email\": \"ana@example.com\"
                  },
                  \"payrollDate\": \"2026-03-01\",
                  \"currency\": \"BRL\",
                  \"items\": [
                    {\"description\": \"Salário\", \"type\": \"CREDIT\", \"amount\": 1000.00},
                    {\"description\": \"Bônus\", \"type\": \"CREDIT\", \"amount\": 250.00},
                    {\"description\": \"INSS\", \"type\": \"DEBIT\", \"amount\": 150.00}
                  ],
                  \"requestedAt\": \"2026-03-18T09:00:00Z\",
                  \"callbackTopic\": \"payroll.generation.result\"
                }
                """;
    }
}
