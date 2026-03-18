package com.example.payroll.infrastructure.storage;

import com.example.payroll.domain.model.PdfDocument;
import com.example.payroll.domain.model.StoredFile;
import com.example.payroll.domain.port.FileStoragePort;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class S3PayrollFileStorageAdapter implements FileStoragePort {
    private final String bucketName;
    private final String publicBaseUrl;
    private final Map<String, byte[]> uploadedObjects = new LinkedHashMap<>();

    public S3PayrollFileStorageAdapter(String bucketName, String publicBaseUrl) {
        this.bucketName = bucketName;
        this.publicBaseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
    }

    @Override
    public StoredFile store(PdfDocument document, String tenantId, String employeeId) {
        String safeFileName = document.fileName().replaceAll("[^a-zA-Z0-9._-]", "-");
        String key = bucketName + "/" + tenantId + "/" + employeeId + "/" + LocalDate.now() + "/" + safeFileName;
        uploadedObjects.put(key, document.content());
        return new StoredFile(key, publicBaseUrl + "/" + key);
    }

    public Map<String, byte[]> uploadedObjects() {
        return uploadedObjects;
    }
}
