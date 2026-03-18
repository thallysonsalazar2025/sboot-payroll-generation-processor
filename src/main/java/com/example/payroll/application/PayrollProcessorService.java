package com.example.payroll.application;

import com.example.payroll.application.dto.PayrollGenerationResultMessage;
import com.example.payroll.application.dto.PayrollNotificationMessage;
import com.example.payroll.application.mapper.PayrollMessageMapper;
import com.example.payroll.domain.model.PayrollDocument;
import com.example.payroll.domain.model.PayrollGenerationRequest;
import com.example.payroll.domain.model.PayrollGenerationResult;
import com.example.payroll.domain.model.PayrollNotification;
import com.example.payroll.domain.model.PayrollProcessingStatus;
import com.example.payroll.domain.model.PdfDocument;
import com.example.payroll.domain.model.StoredFile;
import com.example.payroll.domain.port.FileStoragePort;
import com.example.payroll.domain.port.PayrollDocumentRepository;
import com.example.payroll.domain.port.PdfGeneratorPort;
import com.example.payroll.domain.port.TopicPublisher;
import com.example.payroll.domain.service.PayrollCalculator;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public class PayrollProcessorService {
    private final PdfGeneratorPort pdfGenerator;
    private final FileStoragePort storagePort;
    private final PayrollDocumentRepository repository;
    private final TopicPublisher<PayrollGenerationResultMessage> resultPublisher;
    private final TopicPublisher<PayrollNotificationMessage> notificationPublisher;
    private final PayrollCalculator calculator;
    private final PayrollMessageMapper mapper;
    private final Clock clock;

    public PayrollProcessorService(
            PdfGeneratorPort pdfGenerator,
            FileStoragePort storagePort,
            PayrollDocumentRepository repository,
            TopicPublisher<PayrollGenerationResultMessage> resultPublisher,
            TopicPublisher<PayrollNotificationMessage> notificationPublisher,
            PayrollCalculator calculator,
            PayrollMessageMapper mapper,
            Clock clock) {
        this.pdfGenerator = pdfGenerator;
        this.storagePort = storagePort;
        this.repository = repository;
        this.resultPublisher = resultPublisher;
        this.notificationPublisher = notificationPublisher;
        this.calculator = calculator;
        this.mapper = mapper;
        this.clock = clock;
    }

    public PayrollGenerationResult process(PayrollGenerationRequest request) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        try {
            var gross = calculator.grossAmount(request);
            var discount = calculator.discountAmount(request);
            var net = calculator.netAmount(request);
            PdfDocument pdf = pdfGenerator.generate(request, gross, discount, net);
            StoredFile stored = storagePort.store(pdf, request.tenantId(), request.employee().employeeId());
            PayrollDocument saved = repository.save(new PayrollDocument(
                    request.requestId(), request.tenantId(), request.employee().employeeId(), pdf.fileName(), stored.storageKey(), stored.publicUrl(), gross, discount, net, PayrollProcessingStatus.COMPLETED, now));
            PayrollGenerationResult result = new PayrollGenerationResult(saved.requestId(), saved.tenantId(), saved.employeeId(), saved.status(), saved.fileUrl(), "Payroll generated successfully", now);
            publishSuccess(request.callbackTopic(), result);
            return result;
        } catch (RuntimeException ex) {
            PayrollGenerationResult result = new PayrollGenerationResult(request.requestId(), request.tenantId(), request.employee().employeeId(), PayrollProcessingStatus.FAILED, null, "Payroll generation failed: " + ex.getMessage(), now);
            publishFailure(request.callbackTopic(), result);
            return result;
        }
    }

    public Optional<PayrollDocument> findByRequestId(UUID requestId) {
        return repository.findByRequestId(requestId);
    }

    private void publishSuccess(String callbackTopic, PayrollGenerationResult result) {
        resultPublisher.publish(PayrollTopology.EXG_NAME_PAYROLL_GENERATION, callbackTopic, mapper.toMessage(result));
        notificationPublisher.publish(PayrollTopology.EXG_NAME_PAYROLL_GENERATION, PayrollTopology.NOTIFICATION_TOPIC,
                mapper.toMessage(new PayrollNotification(result.requestId(), PayrollProcessingStatus.COMPLETED, "Payroll request finished successfully", result.processedAt())));
    }

    private void publishFailure(String callbackTopic, PayrollGenerationResult result) {
        resultPublisher.publish(PayrollTopology.EXG_NAME_PAYROLL_GENERATION, callbackTopic, mapper.toMessage(result));
        notificationPublisher.publish(PayrollTopology.EXG_NAME_PAYROLL_GENERATION, PayrollTopology.NOTIFICATION_TOPIC,
                mapper.toMessage(new PayrollNotification(result.requestId(), PayrollProcessingStatus.FAILED, result.message(), result.processedAt())));
    }
}
