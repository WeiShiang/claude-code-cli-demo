package com.example.claudecodeclidemo.catalog.domain.vo;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryIdTest {

    @Test
    void 동일한UUID는같다() {
        var uuid = UUID.randomUUID();
        assertThat(new CategoryId(uuid)).isEqualTo(new CategoryId(uuid));
        assertThat(new CategoryId(uuid).hashCode()).isEqualTo(new CategoryId(uuid).hashCode());
    }

    @Test
    void 다른UUID는다르다() {
        assertThat(new CategoryId(UUID.randomUUID())).isNotEqualTo(new CategoryId(UUID.randomUUID()));
    }

    @Test
    void 비CategoryId객체와는다르다() {
        var id = new CategoryId(UUID.randomUUID());
        assertThat(id).isNotEqualTo("not-a-category-id");
    }

    @Test
    void toString은UUID문자열을반환() {
        var uuid = UUID.randomUUID();
        assertThat(new CategoryId(uuid).toString()).isEqualTo(uuid.toString());
    }

    @Test
    void null값은예외() {
        assertThatThrownBy(() -> new CategoryId(null))
                .isInstanceOf(NullPointerException.class);
    }
}
