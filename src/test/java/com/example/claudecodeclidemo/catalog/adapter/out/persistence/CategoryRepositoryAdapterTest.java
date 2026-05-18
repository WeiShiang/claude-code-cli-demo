package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.domain.entity.Category;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CategoryRepositoryAdapter.class)
class CategoryRepositoryAdapterTest {

    @Autowired CategoryRepositoryAdapter adapter;
    @Autowired EntityManager em;

    @Test
    void save_and_findById_rootCategory() {
        Category root = Category.create("Electronics");
        adapter.save(root);
        em.flush();
        em.clear();

        Optional<Category> found = adapter.findById(root.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Electronics");
        assertThat(found.get().getParentId()).isEmpty();
    }

    @Test
    void save_persistsParentId() {
        Category parent = Category.create("Electronics");
        Category child = Category.of(CategoryId.generate(), "Phones", parent.getId());

        adapter.save(parent);
        adapter.save(child);
        em.flush();
        em.clear();

        Category loaded = adapter.findById(child.getId()).orElseThrow();
        assertThat(loaded.getParentId()).contains(parent.getId());
    }

    @Test
    void save_isUpsertForSameId_andUpdatesName() {
        Category category = Category.create("Old");
        adapter.save(category);
        em.flush();

        category.rename("New");
        adapter.save(category);
        em.flush();
        em.clear();

        Category loaded = adapter.findById(category.getId()).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("New");
        assertThat(adapter.findAll()).hasSize(1);
    }

    @Test
    void findById_returnsEmpty_whenMissing() {
        assertThat(adapter.findById(CategoryId.generate())).isEmpty();
    }

    @Test
    void findAll_returnsEveryCategory() {
        adapter.save(Category.create("A"));
        adapter.save(Category.create("B"));
        em.flush();
        em.clear();

        List<Category> all = adapter.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).extracting(Category::getName).containsExactlyInAnyOrder("A", "B");
    }
}
