package com.example.payroll.infrastructure.persistence;

import com.example.payroll.domain.model.PayrollDocument;
import com.example.payroll.domain.port.PayrollDocumentRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryPayrollDocumentRepository implements PayrollDocumentRepository {
    private final Map<String, PayrollDocument> storage = new LinkedHashMap<>();

    @Override
    public PayrollDocument save(PayrollDocument document) {
        storage.put(key(document.companyId(), document.employeeId(), document.month(), document.year()), document);
        return document;
    }

    @Override
    public Optional<PayrollDocument> findByPayrollPeriod(String companyId, String employeeId, Integer month, Integer year) {
        return Optional.ofNullable(storage.get(key(companyId, employeeId, month, year)));
    }

    public int size() {
        return storage.size();
    }

    private String key(String companyId, String employeeId, Integer month, Integer year) {
        return companyId + ":" + employeeId + ":" + year + ":" + month;
    }
}
