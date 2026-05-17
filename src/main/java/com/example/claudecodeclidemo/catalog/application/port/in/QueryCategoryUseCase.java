package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;

import java.util.List;
import java.util.Optional;

public interface QueryCategoryUseCase {
    CategoryView findById(CategoryId categoryId);
    List<CategoryView> findAll();

    record CategoryView(CategoryId id, String name, Optional<CategoryId> parentId) {}
}
