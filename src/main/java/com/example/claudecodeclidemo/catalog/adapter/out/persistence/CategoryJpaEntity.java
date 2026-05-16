package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "categories")
class CategoryJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "parent_id")
    private UUID parentId;

    protected CategoryJpaEntity() {}

    CategoryJpaEntity(UUID id, String name, UUID parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    UUID getId() { return id; }
    String getName() { return name; }
    UUID getParentId() { return parentId; }
}
