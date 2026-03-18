package com.example.payroll.application;

import com.example.payroll.application.dto.PayrollGenerationResultMessage;
import com.example.payroll.application.dto.PayrollNotificationMessage;
import com.example.payroll.application.mapper.PayrollMessageMapper;
import com.example.payroll.domain.model.EmployeeSnapshot;
import com.example.payroll.domain.model.PayrollGenerationRequest;
import com.example.payroll.domain.model.PayrollItem;
import com.example.payroll.domain.model.PayrollItemType;
import com.example.payroll.domain.model.PayrollProcessingStatus;
import com.example.payroll.domain.model.PdfDocument;
import com.example.payroll.domain.model.StoredFile;
import com.example.payroll.domain.port.FileStoragePort;
import com.example.payroll.domain.port.PdfGeneratorPort;
import com.example.payroll.domain.service.PayrollCalculator;
import com.example.payroll.infrastructure.amqp.InMemoryTopicPublisher;
import com.example.payroll.infrastructure.persistence.InMemoryPayrollDocumentRepository;
import com.example.payroll.support.TestSupport;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

public class PayrollProcessorServiceTest {
    public static void main(String[] args) {
        shouldGenerateStorePersistAndPublish();
        shouldPublishFailureWhenPdfGenerationFails();
        shouldQueryPersistedDocument();
        System.out.println("PayrollProcessorServiceTest OK");
    }

    static void shouldGenerateStorePersistAndPublish() {
        var resultPublisher = new InMemoryTopicPublisher<PayrollGenerationResultMessage>();
        var notificationPublisher = new InMemoryTopicPublisher<PayrollNotificationMessage>();
        var repository = new InMemoryPayrollDocumentRepository();
        PdfGeneratorPort pdfGenerator = (request, gross, discount, net) -> new PdfDocument("pay.pdf", "pdf".getBytes());
        FileStoragePort storage = (document, tenantId, employeeId) -> new StoredFile("bucket/key/pay.pdf", "https://files/pay.pdf");
        var service = new PayrollProcessorService(pdfGenerator, storage, repository, resultPublisher, notificationPublisher,
                new PayrollCalculator(), new PayrollMessageMapper(), Clock.fixed(Instant.parse("2026-03-18T10:15:30Z"), ZoneOffset.UTC));

        var result = service.process(sampleRequest());

        TestSupport.assertEquals(PayrollProcessingStatus.COMPLETED, result.status(), "status should be completed");
        TestSupport.assertEquals(1, repository.size(), "repository should store one document");
        var saved = repository.findByRequestId(sampleRequest().requestId()).orElseThrow();
        TestSupport.assertEquals(new BigDecimal("1250.00"), saved.grossAmount(), "gross amount");
        TestSupport.assertEquals(new BigDecimal("150.00"), saved.discountAmount(), "discount amount");
        TestSupport.assertEquals(new BigDecimal("1100.00"), saved.netAmount(), "net amount");
        TestSupport.assertEquals(PayrollTopology.EXG_NAME_PAYROLL_GENERATION, resultPublisher.messages().getFirst().exchange(), "exchange");
        TestSupport.assertEquals(sampleRequest().callbackTopic(), resultPublisher.messages().getFirst().topic(), "callback topic");
        TestSupport.assertEquals(PayrollTopology.NOTIFICATION_TOPIC, notificationPublisher.messages().getFirst().topic(), "notification topic");
    }

    static void shouldPublishFailureWhenPdfGenerationFails() {
        var resultPublisher = new InMemoryTopicPublisher<PayrollGenerationResultMessage>();
        var notificationPublisher = new InMemoryTopicPublisher<PayrollNotificationMessage>();
        var repository = new InMemoryPayrollDocumentRepository();
        PdfGeneratorPort pdfGenerator = (request, gross, discount, net) -> { throw new IllegalStateException("template missing"); };
        FileStoragePort storage = (document, tenantId, employeeId) -> new StoredFile("ignored", "ignored");
        var service = new PayrollProcessorService(pdfGenerator, storage, repository, resultPublisher, notificationPublisher,
                new PayrollCalculator(), new PayrollMessageMapper(), Clock.systemUTC());

        var result = service.process(sampleRequest());

        TestSupport.assertEquals(PayrollProcessingStatus.FAILED, result.status(), "failure status");
        TestSupport.assertContains("template missing", result.message(), "failure message");
        TestSupport.assertEquals(0, repository.size(), "repository should stay empty");
        TestSupport.assertEquals(PayrollProcessingStatus.FAILED, resultPublisher.messages().getFirst().payload().status(), "result publisher failure");
        TestSupport.assertEquals(PayrollProcessingStatus.FAILED, notificationPublisher.messages().getFirst().payload().status(), "notification failure");
    }

    static void shouldQueryPersistedDocument() {
        var resultPublisher = new InMemoryTopicPublisher<PayrollGenerationResultMessage>();
        var notificationPublisher = new InMemoryTopicPublisher<PayrollNotificationMessage>();
        var repository = new InMemoryPayrollDocumentRepository();
        PdfGeneratorPort pdfGenerator = (request, gross, discount, net) -> new PdfDocument("pay.pdf", "pdf".getBytes());
        FileStoragePort storage = (document, tenantId, employeeId) -> new StoredFile("bucket/key/pay.pdf", "https://files/pay.pdf");
        var service = new PayrollProcessorService(pdfGenerator, storage, repository, resultPublisher, notificationPublisher,
                new PayrollCalculator(), new PayrollMessageMapper(), Clock.systemUTC());
        var request = sampleRequest();
        service.process(request);

        var found = service.findByRequestId(request.requestId());

        TestSupport.assertTrue(found.isPresent(), "document should be found");
        TestSupport.assertEquals(request.requestId(), found.orElseThrow().requestId(), "request id");
    }

    static PayrollGenerationRequest sampleRequest() {
        return new PayrollGenerationRequest(
                UUID.fromString("13ef16ef-eab7-41cd-ac6d-e788a237cbec"),
                "tenant-a",
                new EmployeeSnapshot("emp-1", "Ana Silva", "12345678900", "ana@example.com"),
                LocalDate.of(2026, 3, 1),
                "BRL",
                List.of(
                        new PayrollItem("Salário", PayrollItemType.CREDIT, new BigDecimal("1000.00")),
                        new PayrollItem("Bônus", PayrollItemType.CREDIT, new BigDecimal("250.00")),
                        new PayrollItem("INSS", PayrollItemType.DEBIT, new BigDecimal("150.00"))),
                OffsetDateTime.parse("2026-03-18T09:00:00Z"),
                PayrollTopology.DEFAULT_RESULT_TOPIC);
    }
}
