package com.example.claudecodeclidemo.catalog.application.port.out;

import com.example.claudecodeclidemo.catalog.domain.entity.Category;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;

import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(CategoryId id);
    boolean existsById(CategoryId id);
}
