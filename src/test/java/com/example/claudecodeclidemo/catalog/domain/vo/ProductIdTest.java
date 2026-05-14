package com.example.claudecodeclidemo.catalog.domain.vo;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductIdTest {

    @Test
    void generate로생성된ID는null이아님() {
        assertThat(ProductId.generate()).isNotNull();
        assertThat(ProductId.generate().value()).isNotNull();
    }

    @Test
    void 동일한UUID는같다() {
        var uuid = UUID.randomUUID();
        assertThat(new ProductId(uuid)).isEqualTo(new ProductId(uuid));
        assertThat(new ProductId(uuid).hashCode()).isEqualTo(new ProductId(uuid).hashCode());
    }

    @Test
    void 다른UUID는다르다() {
        assertThat(new ProductId(UUID.randomUUID())).isNotEqualTo(new ProductId(UUID.randomUUID()));
    }

    @Test
    void 비ProductId객체와는다르다() {
        var id = new ProductId(UUID.randomUUID());
        assertThat(id).isNotEqualTo("not-a-product-id");
    }

    @Test
    void toString은UUID문자열을반환() {
        var uuid = UUID.randomUUID();
        assertThat(new ProductId(uuid).toString()).isEqualTo(uuid.toString());
    }

    @Test
    void null값은예외() {
        assertThatThrownBy(() -> new ProductId(null))
                .isInstanceOf(NullPointerException.class);
    }
}
