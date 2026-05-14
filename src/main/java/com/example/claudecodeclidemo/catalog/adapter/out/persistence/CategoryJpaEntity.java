package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class CategoryJpaEntity {

    @Id
    UUID id;

    @Column(nullable = false)
    String name;

    protected CategoryJpaEntity() {}

    public CategoryJpaEntity(UUID id, String name) {
        this.id = id;
        this.name = name;
    }
}
