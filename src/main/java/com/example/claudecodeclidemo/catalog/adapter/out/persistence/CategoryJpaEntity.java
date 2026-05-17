package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "catalog_categories")
public class CategoryJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    private UUID parentId;

    @Column(nullable = false)
    private Instant createdAt;

    protected CategoryJpaEntity() {}

    public CategoryJpaEntity(UUID id, String name, UUID parentId, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getParentId() { return parentId; }
    public Instant getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
}
