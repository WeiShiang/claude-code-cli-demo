package com.example.claudecodeclidemo.catalog.domain.entity;

import com.example.claudecodeclidemo.catalog.domain.exception.InvalidCategoryNameException;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.shared.domain.AggregateRoot;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class Category extends AggregateRoot<CategoryId> {

    private final CategoryId id;
    private String name;
    private final CategoryId parentId;
    private final Instant createdAt;

    private Category(CategoryId id, String name, CategoryId parentId, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = validateName(name);
        this.parentId = parentId;
        this.createdAt = createdAt;
    }

    public static Category create(String name) {
        return new Category(CategoryId.generate(), name, null, Instant.now());
    }

    public static Category reconstitute(CategoryId id, String name, CategoryId parentId, Instant createdAt) {
        return new Category(id, name, parentId, Objects.requireNonNull(createdAt, "createdAt"));
    }

    @Override public CategoryId getId() { return id; }
    public String getName() { return name; }
    public Optional<CategoryId> getParentId() { return Optional.ofNullable(parentId); }
    public Instant getCreatedAt() { return createdAt; }

    public void rename(String newName) {
        this.name = validateName(newName);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidCategoryNameException("name cannot be blank");
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
