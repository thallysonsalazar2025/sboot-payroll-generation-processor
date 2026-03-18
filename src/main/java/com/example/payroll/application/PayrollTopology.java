package com.example.payroll.application;

public final class PayrollTopology {
    public static final String EXG_NAME_PAYROLL_GENERATION = "payroll.generation.direct";
    public static final String QUEUE_NAME_GENERATION_PAYROLL = "payroll.generation.queue.";
    public static final String ROUTING_KEY_PAY_GEN = "payroll.generation.key";
    public static final String DEFAULT_RESULT_TOPIC = "payroll.generation.result";
    public static final String NOTIFICATION_TOPIC = "payroll.notification.status";

    private PayrollTopology() {
    }
}
