package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.in.QueryProductUseCase.ProductView;
import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryProductServiceTest {

    @Mock ProductRepository productRepository;
    @InjectMocks QueryProductService service;

    @Test
    void findById_returns_view() {
        Product p = Product.createDraft(Sku.of("S"), "Name", "desc");
        when(productRepository.findById(p.getId())).thenReturn(Optional.of(p));
        ProductView view = service.findById(p.getId());
        assertThat(view.id()).isEqualTo(p.getId());
        assertThat(view.sku()).isEqualTo(Sku.of("S"));
        assertThat(view.name()).isEqualTo("Name");
    }

    @Test
    void findById_not_found_throws() {
        when(productRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(ProductId.generate()))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void findBySku_returns_view() {
        Product p = Product.createDraft(Sku.of("S"), "Name", "desc");
        when(productRepository.findBySku(Sku.of("S"))).thenReturn(Optional.of(p));
        assertThat(service.findBySku(Sku.of("S")).id()).isEqualTo(p.getId());
    }

    @Test
    void findBySku_not_found_throws() {
        when(productRepository.findBySku(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findBySku(Sku.of("Z")))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void findAll_returns_list() {
        Product p1 = Product.createDraft(Sku.of("A"), "n", "d");
        Product p2 = Product.createDraft(Sku.of("B"), "n", "d");
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));
        assertThat(service.findAll()).hasSize(2);
    }
}
