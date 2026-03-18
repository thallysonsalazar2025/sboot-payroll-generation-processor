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

        var document = repository.findByPayrollPeriod("company-a", "emp-1", 3, 2026).orElseThrow();
        TestSupport.assertEquals("company-a", document.companyId(), "company should match");
        TestSupport.assertEquals(PayrollTopology.EXG_NAME_PAYROLL_GENERATION, resultPublisher.messages().getFirst().exchange(), "result exchange");
        TestSupport.assertEquals(PayrollTopology.NOTIFICATION_TOPIC, notificationPublisher.messages().getFirst().topic(), "notification topic");
        TestSupport.assertTrue(document.fileUrl().contains("https://s3.amazonaws.com/payroll-generated-files"), "stored url should be s3 based");
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
