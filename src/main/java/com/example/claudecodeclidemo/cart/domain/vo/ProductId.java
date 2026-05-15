package com.example.claudecodeclidemo.cart.domain.vo;

import java.util.Objects;
import java.util.UUID;

public final class ProductId {
    private final UUID value;

    public ProductId(UUID value) {
        this.value = Objects.requireNonNull(value, "ProductId value must not be null");
    }

    public UUID value() { return value; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProductId p)) return false;
        return value.equals(p.value);
    }

    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value.toString(); }
}
