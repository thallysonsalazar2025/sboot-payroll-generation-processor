package com.example.payroll.domain.port;

import com.example.payroll.domain.model.PayrollItem;
import java.util.List;

public interface PayrollItemsPort {
    List<PayrollItem> findByPayrollPeriod(String companyId, String employeeId, Integer month, Integer year);
}
