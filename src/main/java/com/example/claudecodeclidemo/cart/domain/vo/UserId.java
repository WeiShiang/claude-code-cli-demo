package com.example.claudecodeclidemo.cart.domain.vo;

import java.util.Objects;
import java.util.UUID;

public final class UserId {
    private final UUID value;

    public UserId(UUID value) {
        this.value = Objects.requireNonNull(value, "UserId value must not be null");
    }

    public UUID value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserId u)) return false;
        return value.equals(u.value);
    }

    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value.toString(); }
}
