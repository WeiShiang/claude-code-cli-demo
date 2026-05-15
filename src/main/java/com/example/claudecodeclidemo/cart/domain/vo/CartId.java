package com.example.claudecodeclidemo.cart.domain.vo;

import java.util.Objects;
import java.util.UUID;

public final class CartId {
    private final UUID value;

    public CartId(UUID value) {
        this.value = Objects.requireNonNull(value, "CartId value must not be null");
    }

    public static CartId generate() {
        return new CartId(UUID.randomUUID());
    }

    public UUID value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CartId c)) return false;
        return value.equals(c.value);
    }

    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value.toString(); }
}
