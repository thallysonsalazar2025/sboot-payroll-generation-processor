package com.example.payroll.domain.port;

import com.example.payroll.domain.model.PayrollDocument;
import java.util.Optional;
import java.util.UUID;

public interface PayrollDocumentRepository {
    PayrollDocument save(PayrollDocument document);
    Optional<PayrollDocument> findByRequestId(UUID requestId);
}
