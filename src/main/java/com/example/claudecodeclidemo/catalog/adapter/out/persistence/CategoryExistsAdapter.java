package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.application.port.out.CategoryRepository;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import org.springframework.stereotype.Component;

@Component
class CategoryExistsAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpa;

    CategoryExistsAdapter(CategoryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean exists(CategoryId categoryId) {
        return jpa.existsById(categoryId.value());
    }
}
