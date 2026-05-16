package com.example.claudecodeclidemo.catalog.domain.vo;

import com.example.claudecodeclidemo.catalog.domain.exception.InvalidPriceException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidSkuException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogVoTest {

    private static final Currency TWD = Currency.getInstance("TWD");

    // ── ProductId ─────────────────────────────────────────────────────────

    @Test
    void productId_withUuid_storesValue() {
        var uuid = UUID.randomUUID();
        var id = new ProductId(uuid);
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    void productId_equality_basedOnValue() {
        var uuid = UUID.randomUUID();
        assertThat(new ProductId(uuid)).isEqualTo(new ProductId(uuid));
    }

    // ── CategoryId ────────────────────────────────────────────────────────

    @Test
    void categoryId_withUuid_storesValue() {
        var uuid = UUID.randomUUID();
        assertThat(new CategoryId(uuid).value()).isEqualTo(uuid);
    }

    // ── Sku ───────────────────────────────────────────────────────────────

    @Test
    void sku_validFormat_creates() {
        assertThat(new Sku("PROD-001").value()).isEqualTo("PROD-001");
    }

    @ParameterizedTest
    @ValueSource(strings = {"AB", "abc", "prod-001", "TOOLONGSKUVALUE12345X", "INVALID SPACE"})
    void sku_invalidFormat_throwsInvalidSkuException(String value) {
        assertThatThrownBy(() -> new Sku(value))
                .isInstanceOf(InvalidSkuException.class);
    }

    @Test
    void sku_exactlyFourChars_creates() {
        assertThat(new Sku("ABCD").value()).isEqualTo("ABCD");
    }

    @Test
    void sku_exactlyTwentyChars_creates() {
        assertThat(new Sku("ABCDEFGHIJ1234567890").value()).isEqualTo("ABCDEFGHIJ1234567890");
    }

    @Test
    void sku_twentyOneChars_throwsInvalidSkuException() {
        assertThatThrownBy(() -> new Sku("ABCDEFGHIJ12345678901"))
                .isInstanceOf(InvalidSkuException.class);
    }

    // ── Money ─────────────────────────────────────────────────────────────

    @Test
    void money_positiveAmount_creates() {
        var money = new Money(new BigDecimal("100.00"), TWD);
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(money.currency()).isEqualTo(TWD);
    }

    @Test
    void money_zeroAmount_creates() {
        assertThat(new Money(BigDecimal.ZERO, TWD).amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void money_negativeAmount_throwsInvalidPriceException() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-0.01"), TWD))
                .isInstanceOf(InvalidPriceException.class);
    }

    @Test
    void money_equality_basedOnAmountAndCurrency() {
        var a = new Money(new BigDecimal("100"), TWD);
        var b = new Money(new BigDecimal("100.00"), TWD);
        assertThat(a).isEqualTo(b);
    }

    // ── ProductStatus ─────────────────────────────────────────────────────

    @Test
    void productStatus_values_containExpected() {
        assertThat(ProductStatus.values())
                .containsExactlyInAnyOrder(
                        ProductStatus.ACTIVE,
                        ProductStatus.INACTIVE,
                        ProductStatus.DISCONTINUED);
    }
}
