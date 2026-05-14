package com.example.claudecodeclidemo.catalog.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

public final class Sku {

    private static final Pattern VALID_PATTERN = Pattern.compile("[A-Z0-9-]{4,20}");

    private final String value;

    public Sku(String value) {
        if (value == null || !VALID_PATTERN.matcher(value).matches()) {
            throw new InvalidSkuException(value);
        }
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sku s)) return false;
        return value.equals(s.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
