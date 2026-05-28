package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.in.CreateCategoryUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.QueryCategoryUseCase;
import com.example.claudecodeclidemo.catalog.application.port.out.CategoryRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Category;
import com.example.claudecodeclidemo.catalog.domain.exception.CategoryNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService implements CreateCategoryUseCase, QueryCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryId createCategory(CreateCategoryCommand command) {
        Category category = Category.create(command.name());
        categoryRepository.save(category);
        return category.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryView findById(CategoryId categoryId) {
        return categoryRepository.findById(categoryId)
                .map(this::toView)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "category not found: " + categoryId.value()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryView> findAll() {
        return categoryRepository.findAll().stream().map(this::toView).toList();
    }

    private CategoryView toView(Category c) {
        return new CategoryView(c.getId(), c.getName(), c.getParentId());
    }
}
