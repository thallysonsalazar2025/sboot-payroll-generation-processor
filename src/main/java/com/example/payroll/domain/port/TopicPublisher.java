package com.example.payroll.domain.port;

public interface TopicPublisher<T> {
    void publish(String exchange, String topic, T payload);
}
