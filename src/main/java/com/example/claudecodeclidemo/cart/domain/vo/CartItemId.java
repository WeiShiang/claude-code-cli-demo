package com.example.claudecodeclidemo.cart.domain.vo;

import java.util.Objects;
import java.util.UUID;

public final class CartItemId {
    private final UUID value;

    public CartItemId(UUID value) {
        this.value = Objects.requireNonNull(value, "CartItemId value must not be null");
    }

    public static CartItemId generate() {
        return new CartItemId(UUID.randomUUID());
    }

    public UUID value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CartItemId c)) return false;
        return value.equals(c.value);
    }

    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value.toString(); }
}
