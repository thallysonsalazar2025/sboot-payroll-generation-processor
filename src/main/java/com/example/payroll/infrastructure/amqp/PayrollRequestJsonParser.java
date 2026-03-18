package com.example.payroll.infrastructure.amqp;

import com.example.payroll.application.dto.PayrollGenerationRequestMessage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayrollRequestJsonParser {
    public PayrollGenerationRequestMessage parse(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("payload must not be blank");
        }
        try {
            var employeeBlock = block(json, "employee");
            var itemsBlock = array(json, "items");
            return new PayrollGenerationRequestMessage(
                    UUID.fromString(string(json, "requestId")),
                    string(json, "tenantId"),
                    new PayrollGenerationRequestMessage.EmployeeMessage(
                            string(employeeBlock, "employeeId"),
                            string(employeeBlock, "employeeName"),
                            string(employeeBlock, "documentNumber"),
                            string(employeeBlock, "email")),
                    LocalDate.parse(string(json, "payrollDate")),
                    string(json, "currency"),
                    parseItems(itemsBlock),
                    OffsetDateTime.parse(string(json, "requestedAt")),
                    string(json, "callbackTopic"));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid payroll generation message payload", ex);
        }
    }

    private List<PayrollGenerationRequestMessage.PayrollItemMessage> parseItems(String itemsBlock) {
        Matcher matcher = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL).matcher(itemsBlock);
        List<PayrollGenerationRequestMessage.PayrollItemMessage> items = new ArrayList<>();
        while (matcher.find()) {
            String item = matcher.group();
            items.add(new PayrollGenerationRequestMessage.PayrollItemMessage(
                    string(item, "description"),
                    string(item, "type"),
                    new BigDecimal(number(item, "amount"))));
        }
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        return items;
    }

    private String string(String json, String field) {
        Matcher matcher = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("missing field " + field);
        }
        return matcher.group(1);
    }

    private String number(String json, String field) {
        Matcher matcher = Pattern.compile("\"" + field + "\"\\s*:\\s*([-0-9.]+)").matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("missing numeric field " + field);
        }
        return matcher.group(1);
    }

    private String block(String json, String field) {
        Matcher matcher = Pattern.compile("\"" + field + "\"\\s*:\\s*\\{(.*?)\\}", Pattern.DOTALL).matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("missing object field " + field);
        }
        return matcher.group();
    }

    private String array(String json, String field) {
        Matcher matcher = Pattern.compile("\"" + field + "\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL).matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("missing array field " + field);
        }
        return matcher.group(1);
    }
}
