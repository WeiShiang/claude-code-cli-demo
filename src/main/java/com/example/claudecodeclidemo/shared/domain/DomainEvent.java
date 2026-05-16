package com.example.claudecodeclidemo.shared.domain;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}
