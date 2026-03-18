package com.example.payroll.infrastructure.persistence;

import com.example.payroll.domain.model.PayrollDocument;
import com.example.payroll.domain.port.PayrollDocumentRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryPayrollDocumentRepository implements PayrollDocumentRepository {
    private final Map<UUID, PayrollDocument> storage = new LinkedHashMap<>();

    @Override
    public PayrollDocument save(PayrollDocument document) {
        storage.put(document.requestId(), document);
        return document;
    }

    @Override
    public Optional<PayrollDocument> findByRequestId(UUID requestId) {
        return Optional.ofNullable(storage.get(requestId));
    }

    public int size() {
        return storage.size();
    }
}
