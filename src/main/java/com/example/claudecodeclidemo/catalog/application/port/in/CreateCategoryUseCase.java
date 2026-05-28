package com.example.claudecodeclidemo.catalog.application.port.in;

import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;

public interface CreateCategoryUseCase {
    CategoryId createCategory(CreateCategoryCommand command);

    record CreateCategoryCommand(String name) {
    }
}
