package com.example.payroll.infrastructure.amqp;

import com.example.payroll.application.dto.PayrollGenerationRequestMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayrollRequestJsonParser {
    public PayrollGenerationRequestMessage parse(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("payload must not be blank");
        }
        try {
            return new PayrollGenerationRequestMessage(
                    string(json, "employeeId"),
                    string(json, "companyId"),
                    string(json, "requesterId"),
                    Integer.parseInt(number(json, "month")),
                    Integer.parseInt(number(json, "year")));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Invalid payroll generation message payload", ex);
        }
    }

    private String string(String json, String field) {
        Matcher matcher = Pattern.compile("\\\"" + field + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("missing field " + field);
        }
        return matcher.group(1);
    }

    private String number(String json, String field) {
        Matcher matcher = Pattern.compile("\\\"" + field + "\\\"\\s*:\\s*([-0-9]+)").matcher(json);
        if (!matcher.find()) {
            throw new IllegalArgumentException("missing numeric field " + field);
        }
        return matcher.group(1);
    }
}
