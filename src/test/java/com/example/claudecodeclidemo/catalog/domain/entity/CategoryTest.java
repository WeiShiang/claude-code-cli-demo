package com.example.claudecodeclidemo.catalog.domain.entity;

import com.example.claudecodeclidemo.catalog.domain.exception.InvalidCategoryNameException;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryTest {

    @Test
    void create_with_name_succeeds() {
        Category c = Category.create("Electronics");
        assertThat(c.getId()).isNotNull();
        assertThat(c.getName()).isEqualTo("Electronics");
        assertThat(c.getParentId()).isEmpty();
    }

    @Test
    void create_with_blank_name_throws() {
        assertThatThrownBy(() -> Category.create("")).isInstanceOf(InvalidCategoryNameException.class);
        assertThatThrownBy(() -> Category.create("   ")).isInstanceOf(InvalidCategoryNameException.class);
        assertThatThrownBy(() -> Category.create(null)).isInstanceOf(InvalidCategoryNameException.class);
    }

    @Test
    void rename_updates_name() {
        Category c = Category.create("A");
        c.rename("B");
        assertThat(c.getName()).isEqualTo("B");
    }

    @Test
    void rename_with_blank_throws() {
        Category c = Category.create("A");
        assertThatThrownBy(() -> c.rename("")).isInstanceOf(InvalidCategoryNameException.class);
    }

    @Test
    void equals_by_id() {
        CategoryId id = CategoryId.generate();
        Category c1 = Category.reconstitute(id, "A", null, java.time.Instant.now());
        Category c2 = Category.reconstitute(id, "B", null, java.time.Instant.now());
        assertThat(c1).isEqualTo(c2);
    }
}
