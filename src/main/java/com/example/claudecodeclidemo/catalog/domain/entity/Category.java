package com.example.claudecodeclidemo.catalog.domain.entity;

import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.shared.domain.AggregateRoot;

import java.util.UUID;

public class Category extends AggregateRoot<CategoryId> {

    private final CategoryId id;
    private final String name;
    private final CategoryId parentId;

    private Category(CategoryId id, String name, CategoryId parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    public static Category create(String name) {
        return new Category(new CategoryId(UUID.randomUUID()), name, null);
    }

    public static Category createChild(String name, CategoryId parentId) {
        return new Category(new CategoryId(UUID.randomUUID()), name, parentId);
    }

    public static Category reconstitute(CategoryId id, String name, CategoryId parentId) {
        return new Category(id, name, parentId);
    }

    @Override public CategoryId getId() { return id; }
    public String getName() { return name; }
    public CategoryId getParentId() { return parentId; }
    public boolean isRoot() { return parentId == null; }
}
