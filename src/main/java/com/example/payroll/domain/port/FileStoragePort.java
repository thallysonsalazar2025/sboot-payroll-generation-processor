package com.example.payroll.domain.port;

import com.example.payroll.domain.model.PdfDocument;
import com.example.payroll.domain.model.StoredFile;

public interface FileStoragePort {
    StoredFile store(PdfDocument document, String tenantId, String employeeId);
}
