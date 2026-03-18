package com.example.payroll.infrastructure.pdf;

import com.example.payroll.domain.model.PayrollGenerationRequest;
import com.example.payroll.support.TestSupport;
import java.math.BigDecimal;

public class SimplePdfGeneratorAdapterTest {
    public static void main(String[] args) {
        shouldGenerateUniqueFileNamesForRepeatedRequests();
        System.out.println("SimplePdfGeneratorAdapterTest OK");
    }

    static void shouldGenerateUniqueFileNamesForRepeatedRequests() {
        var generator = new SimplePdfGeneratorAdapter();
        var storage = new com.example.payroll.infrastructure.storage.S3PayrollFileStorageAdapter("payroll-generated-files", "https://s3.amazonaws.com");
        var request = new PayrollGenerationRequest("emp-1", "company-a", "requester-9", 3, 2026);

        var first = generator.generate(request, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.valueOf(9));
        var second = generator.generate(request, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.valueOf(9));

        TestSupport.assertTrue(first.fileName().startsWith("payslip-emp-1-2026-03-"), "first filename should use payslip prefix");
        TestSupport.assertTrue(second.fileName().startsWith("payslip-emp-1-2026-03-"), "second filename should use payslip prefix");
        TestSupport.assertTrue(first.fileName().endsWith(".pdf"), "first filename should use pdf extension");
        TestSupport.assertTrue(second.fileName().endsWith(".pdf"), "second filename should use pdf extension");
        TestSupport.assertTrue(!first.fileName().equals(second.fileName()), "repeated generations should produce unique filenames");

        var firstStored = storage.store(first, request.companyId(), request.employeeId());
        var secondStored = storage.store(second, request.companyId(), request.employeeId());

        TestSupport.assertTrue(!firstStored.storageKey().equals(secondStored.storageKey()), "storage keys should differ for repeated generations");
        TestSupport.assertEquals(2, storage.uploadedObjects().size(), "storage should keep both generated files");
    }
}
