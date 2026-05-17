package com.example.claudecodeclidemo.catalog.application.port.out;

import com.example.claudecodeclidemo.shared.domain.DomainEvent;

import java.util.List;

public interface DomainEventPublisher {
    void publish(DomainEvent event);

    default void publishAll(List<DomainEvent> events) {
        for (DomainEvent e : events) {
            publish(e);
        }
    }
}
