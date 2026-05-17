package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.in.QueryCategoryUseCase.CategoryView;
import com.example.claudecodeclidemo.catalog.application.port.out.CategoryRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Category;
import com.example.claudecodeclidemo.catalog.domain.exception.CategoryNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @InjectMocks CategoryService service;

    @Test
    void createCategory_saves_and_returns_id() {
        CategoryId id = service.createCategory("Electronics");
        assertThat(id).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void findById_returns_view() {
        Category c = Category.create("X");
        when(categoryRepository.findById(c.getId())).thenReturn(Optional.of(c));
        CategoryView view = service.findById(c.getId());
        assertThat(view.name()).isEqualTo("X");
    }

    @Test
    void findById_not_found_throws() {
        when(categoryRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(CategoryId.generate()))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void findAll_returns_list() {
        when(categoryRepository.findAll()).thenReturn(List.of(Category.create("A"), Category.create("B")));
        assertThat(service.findAll()).hasSize(2);
    }
}
