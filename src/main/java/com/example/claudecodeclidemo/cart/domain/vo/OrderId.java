package com.example.claudecodeclidemo.cart.domain.vo;

import java.util.Objects;
import java.util.UUID;

public final class OrderId {
    private final UUID value;

    public OrderId(UUID value) {
        this.value = Objects.requireNonNull(value, "OrderId value must not be null");
    }

    public UUID value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OrderId o2)) return false;
        return value.equals(o2.value);
    }

    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value.toString(); }
}
