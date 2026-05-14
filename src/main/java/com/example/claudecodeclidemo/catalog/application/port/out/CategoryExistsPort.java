package com.example.claudecodeclidemo.catalog.application.port.out;

import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;

public interface CategoryExistsPort {
    boolean exists(CategoryId categoryId);
}
