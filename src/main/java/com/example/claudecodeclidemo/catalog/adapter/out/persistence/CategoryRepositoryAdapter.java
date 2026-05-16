package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.application.port.out.CategoryRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Category;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class CategoryRepositoryAdapter implements CategoryRepository {

    private final CategoryJpaRepository jpaRepository;

    CategoryRepositoryAdapter(CategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Category save(Category category) {
        var parentId = category.getParentId() != null ? category.getParentId().value() : null;
        jpaRepository.save(new CategoryJpaEntity(category.getId().value(), category.getName(), parentId));
        return category;
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return jpaRepository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public boolean existsById(CategoryId id) {
        return jpaRepository.existsById(id.value());
    }

    private Category toDomain(CategoryJpaEntity e) {
        var parentId = e.getParentId() != null ? new CategoryId(e.getParentId()) : null;
        return Category.reconstitute(new CategoryId(e.getId()), e.getName(), parentId);
    }
}
