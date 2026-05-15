package com.example.claudecodeclidemo.cart.domain.vo;

import com.example.claudecodeclidemo.cart.domain.exception.InvalidQuantityException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuantityTest {

    @Test
    void create_withValueOne_succeeds() {
        var q = new Quantity(1);
        assertThat(q.value()).isEqualTo(1);
    }

    @Test
    void create_withPositiveValue_succeeds() {
        var q = new Quantity(10);
        assertThat(q.value()).isEqualTo(10);
    }

    // ── Invariant C-3: quantity must be >= 1 ─────────────────────────────

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    void create_withValueLessThanOne_throwsInvalidQuantityException(int value) {
        assertThatThrownBy(() -> new Quantity(value))
                .isInstanceOf(InvalidQuantityException.class);
    }

    // ── equality ─────────────────────────────────────────────────────────

    @Test
    void equals_sameValue_returnsTrue() {
        var a = new Quantity(5);
        var b = new Quantity(5);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void equals_differentValue_returnsFalse() {
        var a = new Quantity(3);
        var b = new Quantity(5);
        assertThat(a).isNotEqualTo(b);
    }

    // ── add ──────────────────────────────────────────────────────────────

    @Test
    void add_twoQuantities_returnsSum() {
        var a = new Quantity(3);
        var b = new Quantity(2);
        assertThat(a.add(b)).isEqualTo(new Quantity(5));
    }
}
