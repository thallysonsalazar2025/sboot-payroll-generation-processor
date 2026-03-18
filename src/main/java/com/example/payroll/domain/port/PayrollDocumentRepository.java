package com.example.payroll.domain.port;

import com.example.payroll.domain.model.PayrollDocument;
import java.util.Optional;

public interface PayrollDocumentRepository {
    PayrollDocument save(PayrollDocument document);
    Optional<PayrollDocument> findByPayrollPeriod(String companyId, String employeeId, Integer month, Integer year);
}
