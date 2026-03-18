package com.example.payroll.application;

import com.example.payroll.application.dto.PayrollGenerationRequestMessage;
import com.example.payroll.application.mapper.PayrollMessageMapper;
import com.example.payroll.domain.model.PayrollItemType;
import com.example.payroll.support.TestSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class PayrollMessageMapperTest {
    public static void main(String[] args) {
        shouldMapMessageToDomain();
        System.out.println("PayrollMessageMapperTest OK");
    }

    static void shouldMapMessageToDomain() {
        var mapper = new PayrollMessageMapper();
        var message = new PayrollGenerationRequestMessage(
                UUID.fromString("13ef16ef-eab7-41cd-ac6d-e788a237cbec"),
                "tenant-a",
                new PayrollGenerationRequestMessage.EmployeeMessage("emp-1", "Ana Silva", "12345678900", "ana@example.com"),
                LocalDate.of(2026, 3, 1),
                "BRL",
                List.of(new PayrollGenerationRequestMessage.PayrollItemMessage("Salário", "credit", new BigDecimal("1000.00"))),
                OffsetDateTime.parse("2026-03-18T09:00:00Z"),
                PayrollTopology.DEFAULT_RESULT_TOPIC);

        var domain = mapper.toDomain(message);

        TestSupport.assertEquals(message.requestId(), domain.requestId(), "requestId");
        TestSupport.assertEquals("Ana Silva", domain.employee().employeeName(), "employee name");
        TestSupport.assertEquals(PayrollItemType.CREDIT, domain.items().getFirst().type(), "item type");
    }
}
