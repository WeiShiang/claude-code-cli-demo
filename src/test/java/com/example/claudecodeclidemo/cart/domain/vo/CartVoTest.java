package com.example.claudecodeclidemo.cart.domain.vo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CartVoTest {

    private static final Currency TWD = Currency.getInstance("TWD");

    // ── CartId ───────────────────────────────────────────────────────────

    @Test
    void cartId_equals_sameValue_returnsTrue() {
        var uuid = UUID.randomUUID();
        assertThat(new CartId(uuid)).isEqualTo(new CartId(uuid));
    }

    @Test
    void cartId_toString_containsUuid() {
        var uuid = UUID.randomUUID();
        assertThat(new CartId(uuid).toString()).isEqualTo(uuid.toString());
    }

    @Test
    void cartId_hashCode_sameForEqualIds() {
        var uuid = UUID.randomUUID();
        assertThat(new CartId(uuid).hashCode()).isEqualTo(new CartId(uuid).hashCode());
    }

    // ── CartItemId ────────────────────────────────────────────────────────

    @Test
    void cartItemId_equals_sameValue_returnsTrue() {
        var uuid = UUID.randomUUID();
        assertThat(new CartItemId(uuid)).isEqualTo(new CartItemId(uuid));
    }

    @Test
    void cartItemId_toString_containsUuid() {
        var uuid = UUID.randomUUID();
        assertThat(new CartItemId(uuid).toString()).isEqualTo(uuid.toString());
    }

    @Test
    void cartItemId_hashCode_sameForEqualIds() {
        var uuid = UUID.randomUUID();
        assertThat(new CartItemId(uuid).hashCode()).isEqualTo(new CartItemId(uuid).hashCode());
    }

    // ── UserId ────────────────────────────────────────────────────────────

    @Test
    void userId_equals_sameValue_returnsTrue() {
        var uuid = UUID.randomUUID();
        assertThat(new UserId(uuid)).isEqualTo(new UserId(uuid));
    }

    @Test
    void userId_toString_containsUuid() {
        var uuid = UUID.randomUUID();
        assertThat(new UserId(uuid).toString()).isEqualTo(uuid.toString());
    }

    // ── ProductId ─────────────────────────────────────────────────────────

    @Test
    void productId_equals_sameValue_returnsTrue() {
        var uuid = UUID.randomUUID();
        assertThat(new ProductId(uuid)).isEqualTo(new ProductId(uuid));
    }

    @Test
    void productId_toString_containsUuid() {
        var uuid = UUID.randomUUID();
        assertThat(new ProductId(uuid).toString()).isEqualTo(uuid.toString());
    }

    // ── OrderId ───────────────────────────────────────────────────────────

    @Test
    void orderId_equals_sameValue_returnsTrue() {
        var uuid = UUID.randomUUID();
        assertThat(new OrderId(uuid)).isEqualTo(new OrderId(uuid));
    }

    @Test
    void orderId_toString_containsUuid() {
        var uuid = UUID.randomUUID();
        assertThat(new OrderId(uuid).toString()).isEqualTo(uuid.toString());
    }

    // ── Money ─────────────────────────────────────────────────────────────

    @Test
    void money_toString_containsAmountAndCurrency() {
        var money = new Money(new BigDecimal("100"), TWD);
        assertThat(money.toString()).contains("100").contains("TWD");
    }

    @Test
    void money_equals_sameAmountAndCurrency_returnsTrue() {
        var a = new Money(new BigDecimal("100"), TWD);
        var b = new Money(new BigDecimal("100"), TWD);
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void money_equals_differentAmount_returnsFalse() {
        var a = new Money(new BigDecimal("100"), TWD);
        var b = new Money(new BigDecimal("200"), TWD);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void quantity_toString_returnsValue() {
        assertThat(new Quantity(5).toString()).isEqualTo("5");
    }
}
