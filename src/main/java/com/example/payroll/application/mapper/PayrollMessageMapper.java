package com.example.payroll.application.mapper;

import com.example.payroll.application.dto.PayrollGenerationRequestMessage;
import com.example.payroll.application.dto.PayrollGenerationResultMessage;
import com.example.payroll.application.dto.PayrollNotificationMessage;
import com.example.payroll.domain.model.EmployeeSnapshot;
import com.example.payroll.domain.model.PayrollGenerationRequest;
import com.example.payroll.domain.model.PayrollGenerationResult;
import com.example.payroll.domain.model.PayrollItem;
import com.example.payroll.domain.model.PayrollItemType;
import com.example.payroll.domain.model.PayrollNotification;

public class PayrollMessageMapper {
    public PayrollGenerationRequest toDomain(PayrollGenerationRequestMessage source) {
        return new PayrollGenerationRequest(
                source.requestId(),
                source.tenantId(),
                new EmployeeSnapshot(
                        source.employee().employeeId(),
                        source.employee().employeeName(),
                        source.employee().documentNumber(),
                        source.employee().email()),
                source.payrollDate(),
                source.currency(),
                source.items().stream()
                        .map(item -> new PayrollItem(item.description(), PayrollItemType.from(item.type()), item.amount()))
                        .toList(),
                source.requestedAt(),
                source.callbackTopic());
    }

    public PayrollGenerationResultMessage toMessage(PayrollGenerationResult result) {
        return new PayrollGenerationResultMessage(result.requestId(), result.tenantId(), result.employeeId(), result.status(), result.fileUrl(), result.message(), result.processedAt());
    }

    public PayrollNotificationMessage toMessage(PayrollNotification notification) {
        return new PayrollNotificationMessage(notification.requestId(), notification.status(), notification.detail(), notification.notifiedAt());
    }
}
