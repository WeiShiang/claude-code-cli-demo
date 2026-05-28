package com.example.claudecodeclidemo.catalog.adapter.in.web;

import com.example.claudecodeclidemo.catalog.application.port.in.CreateCategoryUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.CreateCategoryUseCase.CreateCategoryCommand;
import com.example.claudecodeclidemo.catalog.application.port.in.QueryCategoryUseCase;
import com.example.claudecodeclidemo.catalog.application.port.in.QueryCategoryUseCase.CategoryView;
import com.example.claudecodeclidemo.catalog.domain.exception.CategoryNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidCategoryNameException;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CategoryController.class, CatalogExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean CreateCategoryUseCase createCategory;
    @MockitoBean QueryCategoryUseCase queryCategory;

    @Test
    void create_returnsId() throws Exception {
        UUID generated = UUID.randomUUID();
        when(createCategory.createCategory(new CreateCategoryCommand("Electronics")))
                .thenReturn(CategoryId.of(generated));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Electronics\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(generated.toString()));
        verify(createCategory).createCategory(new CreateCategoryCommand("Electronics"));
    }

    @Test
    void create_blankName_returns400() throws Exception {
        when(createCategory.createCategory(any()))
                .thenThrow(new InvalidCategoryNameException("blank"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("InvalidCategoryNameException"));
    }

    @Test
    void findById_returnsCategoryView() throws Exception {
        UUID id = UUID.randomUUID();
        UUID parent = UUID.randomUUID();
        CategoryView view = new CategoryView(
                CategoryId.of(id), "Phones", Optional.of(CategoryId.of(parent)));
        when(queryCategory.findById(CategoryId.of(id))).thenReturn(view);

        mockMvc.perform(get("/api/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Phones"))
                .andExpect(jsonPath("$.parentId").value(parent.toString()));
    }

    @Test
    void findById_rootCategory_returnsNullParent() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryView view = new CategoryView(CategoryId.of(id), "Root", Optional.empty());
        when(queryCategory.findById(CategoryId.of(id))).thenReturn(view);

        mockMvc.perform(get("/api/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentId").doesNotExist());
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(queryCategory.findById(any())).thenThrow(new CategoryNotFoundException("no"));

        mockMvc.perform(get("/api/categories/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CategoryNotFoundException"));
    }

    @Test
    void findAll_returnsList() throws Exception {
        UUID id = UUID.randomUUID();
        when(queryCategory.findAll()).thenReturn(List.of(
                new CategoryView(CategoryId.of(id), "Root", Optional.empty())));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value("Root"));
    }
}
