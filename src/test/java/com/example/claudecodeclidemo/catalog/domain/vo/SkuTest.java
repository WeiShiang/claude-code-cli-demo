package com.example.claudecodeclidemo.catalog.domain.vo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkuTest {

    @ParameterizedTest
    @ValueSource(strings = {"PROD-001", "APPLE", "SKU-ABC-123", "AAAA", "A1B2-C3D4"})
    void 合法格式可建立(String value) {
        var sku = new Sku(value);
        assertThat(sku.value()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "ABC",
            "a-lowercase",
            "SKU 001",
            "AAAAAAAAAAAAAAAAAAAAAA"
    })
    void 非法格式拋出InvalidSkuException(String value) {
        assertThatThrownBy(() -> new Sku(value))
                .isInstanceOf(InvalidSkuException.class);
    }

    @Test
    void null值拋出InvalidSkuException() {
        assertThatThrownBy(() -> new Sku(null))
                .isInstanceOf(InvalidSkuException.class);
    }

    @Test
    void 相同值的Sku相等() {
        var a = new Sku("PROD-001");
        var b = new Sku("PROD-001");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void 不同值的Sku不相等() {
        var a = new Sku("PROD-001");
        var b = new Sku("PROD-002");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void toString回傳SKU值() {
        var sku = new Sku("PROD-001");
        assertThat(sku.toString()).isEqualTo("PROD-001");
    }

    @Test
    void 與非Sku物件不相等() {
        var sku = new Sku("PROD-001");
        assertThat(sku).isNotEqualTo("PROD-001");
    }
}
