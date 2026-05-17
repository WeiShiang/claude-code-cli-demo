package com.example.claudecodeclidemo.shared.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AggregateRoot<ID> {

    private final List<DomainEvent> pending = new ArrayList<>();

    public abstract ID getId();

    protected void registerEvent(DomainEvent event) {
        pending.add(Objects.requireNonNull(event, "event"));
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> snapshot = List.copyOf(pending);
        pending.clear();
        return snapshot;
    }

    public boolean hasPendingEvents() {
        return !pending.isEmpty();
    }
}
