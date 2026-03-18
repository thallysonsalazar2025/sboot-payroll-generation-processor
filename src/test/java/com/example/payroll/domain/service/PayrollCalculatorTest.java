package com.example.payroll.domain.service;

import com.example.payroll.domain.model.PayrollGenerationRequest;
import com.example.payroll.domain.model.PayrollItem;
import com.example.payroll.domain.model.PayrollItemType;
import com.example.payroll.support.InMemoryPayrollItemsPort;
import com.example.payroll.support.TestSupport;
import java.math.BigDecimal;
import java.util.List;

public class PayrollCalculatorTest {
    public static void main(String[] args) {
        shouldCalculateGrossDiscountAndNetAmountsFromPayrollItems();
        shouldRejectRequestsWithoutPayrollItems();
        System.out.println("PayrollCalculatorTest OK");
    }

    static void shouldCalculateGrossDiscountAndNetAmountsFromPayrollItems() {
        var itemsPort = new InMemoryPayrollItemsPort().put("company-a", "emp-1", 3, 2026, List.of(
                new PayrollItem("Base salary", PayrollItemType.CREDIT, new BigDecimal("3500.00")),
                new PayrollItem("Bonus", PayrollItemType.CREDIT, new BigDecimal("250.00")),
                new PayrollItem("INSS", PayrollItemType.DEBIT, new BigDecimal("410.00")),
                new PayrollItem("Meal voucher", PayrollItemType.DEBIT, new BigDecimal("90.00"))));
        var calculator = new PayrollCalculator(itemsPort);
        var request = new PayrollGenerationRequest("emp-1", "company-a", "requester-9", 3, 2026);

        TestSupport.assertEquals("3750.00", calculator.grossAmount(request).toPlainString(), "gross amount");
        TestSupport.assertEquals("500.00", calculator.discountAmount(request).toPlainString(), "discount amount");
        TestSupport.assertEquals("3250.00", calculator.netAmount(request).toPlainString(), "net amount");
    }

    static void shouldRejectRequestsWithoutPayrollItems() {
        var calculator = new PayrollCalculator(new InMemoryPayrollItemsPort());
        var request = new PayrollGenerationRequest("emp-404", "company-a", "requester-9", 3, 2026);

        TestSupport.expectThrows(IllegalStateException.class, () -> calculator.grossAmount(request), "missing payroll items should fail");
    }
}
