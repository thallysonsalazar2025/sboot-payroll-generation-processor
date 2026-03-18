package com.example.payroll.application;

import com.example.payroll.application.dto.PayrollGenerationResultMessage;
import com.example.payroll.application.dto.PayrollNotificationMessage;
import com.example.payroll.application.mapper.PayrollMessageMapper;
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
import com.example.payroll.support.InMemoryPayrollItemsPort;
import com.example.payroll.support.TestSupport;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

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
        var itemsPort = payrollItems();
        var service = new PayrollProcessorService(pdfGenerator, storage, repository, resultPublisher, notificationPublisher,
                new PayrollCalculator(itemsPort), new PayrollMessageMapper(), Clock.fixed(Instant.parse("2026-03-18T10:15:30Z"), ZoneOffset.UTC));

        var request = sampleRequest();
        var result = service.process(request);

        TestSupport.assertEquals(PayrollProcessingStatus.COMPLETED, result.status(), "status should be completed");
        TestSupport.assertEquals(1, repository.size(), "repository should store one document");
        var saved = repository.findByPayrollPeriod(request.companyId(), request.employeeId(), request.month(), request.year()).orElseThrow();
        TestSupport.assertEquals("3750.00", saved.grossAmount().toPlainString(), "gross amount");
        TestSupport.assertEquals("500.00", saved.discountAmount().toPlainString(), "discount amount");
        TestSupport.assertEquals("3250.00", saved.netAmount().toPlainString(), "net amount");
        TestSupport.assertEquals(PayrollTopology.EXG_NAME_PAYROLL_GENERATION, resultPublisher.messages().getFirst().exchange(), "exchange");
        TestSupport.assertEquals(PayrollTopology.DEFAULT_RESULT_TOPIC, resultPublisher.messages().getFirst().topic(), "callback topic");
        TestSupport.assertEquals(PayrollTopology.NOTIFICATION_TOPIC, notificationPublisher.messages().getFirst().topic(), "notification topic");
    }

    static void shouldPublishFailureWhenPdfGenerationFails() {
        var resultPublisher = new InMemoryTopicPublisher<PayrollGenerationResultMessage>();
        var notificationPublisher = new InMemoryTopicPublisher<PayrollNotificationMessage>();
        var repository = new InMemoryPayrollDocumentRepository();
        PdfGeneratorPort pdfGenerator = (request, gross, discount, net) -> { throw new IllegalStateException("template missing"); };
        FileStoragePort storage = (document, tenantId, employeeId) -> new StoredFile("ignored", "ignored");
        var itemsPort = payrollItems();
        var service = new PayrollProcessorService(pdfGenerator, storage, repository, resultPublisher, notificationPublisher,
                new PayrollCalculator(itemsPort), new PayrollMessageMapper(), Clock.systemUTC());

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
        var itemsPort = payrollItems();
        var service = new PayrollProcessorService(pdfGenerator, storage, repository, resultPublisher, notificationPublisher,
                new PayrollCalculator(itemsPort), new PayrollMessageMapper(), Clock.systemUTC());
        var request = sampleRequest();
        service.process(request);

        var found = service.findByPayrollPeriod(request.companyId(), request.employeeId(), request.month(), request.year());

        TestSupport.assertTrue(found.isPresent(), "document should be found");
        TestSupport.assertEquals(request.month(), found.orElseThrow().month(), "month");
        TestSupport.assertEquals(request.year(), found.orElseThrow().year(), "year");
    }

    static PayrollGenerationRequest sampleRequest() {
        return new PayrollGenerationRequest("emp-1", "company-a", "requester-9", 3, 2026);
    }

    static InMemoryPayrollItemsPort payrollItems() {
        return new InMemoryPayrollItemsPort().put("company-a", "emp-1", 3, 2026, List.of(
                new PayrollItem("Base salary", PayrollItemType.CREDIT, new BigDecimal("3500.00")),
                new PayrollItem("Bonus", PayrollItemType.CREDIT, new BigDecimal("250.00")),
                new PayrollItem("INSS", PayrollItemType.DEBIT, new BigDecimal("410.00")),
                new PayrollItem("Meal voucher", PayrollItemType.DEBIT, new BigDecimal("90.00"))));
    }
}
