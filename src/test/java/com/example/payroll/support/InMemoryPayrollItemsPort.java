package com.example.payroll.support;

import com.example.payroll.domain.model.PayrollItem;
import com.example.payroll.domain.port.PayrollItemsPort;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InMemoryPayrollItemsPort implements PayrollItemsPort {
    private final Map<String, List<PayrollItem>> storage = new LinkedHashMap<>();

    public InMemoryPayrollItemsPort put(String companyId, String employeeId, Integer month, Integer year, List<PayrollItem> items) {
        storage.put(key(companyId, employeeId, month, year), List.copyOf(items));
        return this;
    }

    @Override
    public List<PayrollItem> findByPayrollPeriod(String companyId, String employeeId, Integer month, Integer year) {
        return storage.getOrDefault(key(companyId, employeeId, month, year), List.of());
    }

    private String key(String companyId, String employeeId, Integer month, Integer year) {
        return companyId + ":" + employeeId + ":" + year + ":" + month;
    }
}
