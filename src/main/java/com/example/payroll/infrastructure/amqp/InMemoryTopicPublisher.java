package com.example.payroll.infrastructure.amqp;

import com.example.payroll.domain.port.TopicPublisher;
import java.util.ArrayList;
import java.util.List;

public class InMemoryTopicPublisher<T> implements TopicPublisher<T> {
    private final List<PublishedMessage<T>> messages = new ArrayList<>();

    @Override
    public void publish(String exchange, String topic, T payload) {
        messages.add(new PublishedMessage<>(exchange, topic, payload));
    }

    public List<PublishedMessage<T>> messages() {
        return messages;
    }

    public record PublishedMessage<T>(String exchange, String topic, T payload) {}
}
