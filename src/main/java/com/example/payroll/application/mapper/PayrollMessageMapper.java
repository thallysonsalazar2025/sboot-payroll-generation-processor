package com.example.payroll.application.mapper;

import com.example.payroll.application.dto.PayrollGenerationRequestMessage;
import com.example.payroll.application.dto.PayrollGenerationResultMessage;
import com.example.payroll.application.dto.PayrollNotificationMessage;
import com.example.payroll.domain.model.PayrollGenerationRequest;
import com.example.payroll.domain.model.PayrollGenerationResult;
import com.example.payroll.domain.model.PayrollNotification;

public class PayrollMessageMapper {
    public PayrollGenerationRequest toDomain(PayrollGenerationRequestMessage source) {
        return new PayrollGenerationRequest(
                source.employeeId(),
                source.companyId(),
                source.requesterId(),
                source.month(),
                source.year());
    }

    public PayrollGenerationResultMessage toMessage(PayrollGenerationResult result) {
        return new PayrollGenerationResultMessage(
                result.companyId(),
                result.employeeId(),
                result.requesterId(),
                result.month(),
                result.year(),
                result.status(),
                result.fileUrl(),
                result.message(),
                result.processedAt());
    }

    public PayrollNotificationMessage toMessage(PayrollNotification notification) {
        return new PayrollNotificationMessage(
                notification.companyId(),
                notification.employeeId(),
                notification.requesterId(),
                notification.month(),
                notification.year(),
                notification.status(),
                notification.detail(),
                notification.notifiedAt());
    }
}
