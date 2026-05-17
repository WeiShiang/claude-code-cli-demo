package com.example.claudecodeclidemo.catalog.application.port.out;

import com.example.claudecodeclidemo.catalog.domain.entity.Category;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    void save(Category category);
    Optional<Category> findById(CategoryId id);
    List<Category> findAll();
}
