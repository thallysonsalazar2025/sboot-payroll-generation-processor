package com.example.payroll.infrastructure.amqp;

import com.example.payroll.application.PayrollProcessorService;
import com.example.payroll.application.mapper.PayrollMessageMapper;

public class PayrollGenerationListener {
    private final PayrollRequestJsonParser parser;
    private final PayrollMessageMapper mapper;
    private final PayrollProcessorService processorService;

    public PayrollGenerationListener(PayrollRequestJsonParser parser, PayrollMessageMapper mapper, PayrollProcessorService processorService) {
        this.parser = parser;
        this.mapper = mapper;
        this.processorService = processorService;
    }

    public void onMessage(String payload) {
        var message = parser.parse(payload);
        processorService.process(mapper.toDomain(message));
    }
}
