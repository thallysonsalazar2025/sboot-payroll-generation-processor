package com.example.payroll.application;

import com.example.payroll.application.dto.PayrollGenerationRequestMessage;
import com.example.payroll.application.mapper.PayrollMessageMapper;
import com.example.payroll.support.TestSupport;

public class PayrollMessageMapperTest {
    public static void main(String[] args) {
        shouldMapMessageToDomain();
        System.out.println("PayrollMessageMapperTest OK");
    }

    static void shouldMapMessageToDomain() {
        var mapper = new PayrollMessageMapper();
        var message = new PayrollGenerationRequestMessage("emp-1", "company-a", "requester-9", 3, 2026);

        var domain = mapper.toDomain(message);

        TestSupport.assertEquals(message.employeeId(), domain.employeeId(), "employeeId");
        TestSupport.assertEquals(message.companyId(), domain.companyId(), "companyId");
        TestSupport.assertEquals(message.requesterId(), domain.requesterId(), "requesterId");
        TestSupport.assertEquals(message.month(), domain.month(), "month");
        TestSupport.assertEquals(message.year(), domain.year(), "year");
    }
}
