package com.example.claudecodeclidemo.cart.domain.vo;

import com.example.claudecodeclidemo.cart.domain.exception.InvalidQuantityException;

import java.util.Objects;

public final class Quantity {
    private final int value;

    public Quantity(int value) {
        if (value < 1) throw new InvalidQuantityException(value);
        this.value = value;
    }

    public int value() { return value; }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Quantity q)) return false;
        return value == q.value;
    }

    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return String.valueOf(value); }
}
