package com.example.claudecodeclidemo.catalog.application.port.out;

import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;

public interface CategoryRepository {
    boolean exists(CategoryId categoryId);
}
