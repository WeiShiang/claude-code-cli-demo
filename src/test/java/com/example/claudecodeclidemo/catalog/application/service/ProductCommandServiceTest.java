package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductUseCase.CreateProductCommand;
import com.example.claudecodeclidemo.catalog.application.port.in.UpdateProductDetailsUseCase.UpdateProductDetailsCommand;
import com.example.claudecodeclidemo.catalog.application.port.out.DomainEventPublisher;
import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.event.UnpublishReason;
import com.example.claudecodeclidemo.catalog.domain.exception.DuplicateSkuException;
import com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductStatus;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCommandServiceTest {

    private static final Currency TWD = Currency.getInstance("TWD");

    @Mock ProductRepository productRepository;
    @Mock DomainEventPublisher eventPublisher;
    @InjectMocks ProductCommandService service;

    @Test
    void createProduct_happy_path_saves_and_publishes() {
        when(productRepository.existsBySku(any())).thenReturn(false);
        ProductId id = service.createProduct(new CreateProductCommand(Sku.of("A-1"), "Name", "desc"));
        assertThat(id).isNotNull();
        verify(productRepository).save(any(Product.class));
        verify(eventPublisher).publishAll(anyList());
    }

    // CAT-INV-001
    @Test
    void createProduct_duplicate_sku_throws() {
        when(productRepository.existsBySku(Sku.of("A-1"))).thenReturn(true);
        assertThatThrownBy(() -> service.createProduct(
                new CreateProductCommand(Sku.of("A-1"), "Name", "desc")))
                .isInstanceOf(DuplicateSkuException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void publishProduct_happy_path() {
        Product p = priced("S-1");
        when(productRepository.findById(p.getId())).thenReturn(Optional.of(p));
        service.publishProduct(p.getId());
        assertThat(p.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
        verify(productRepository).save(p);
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    void publishProduct_not_found_throws() {
        when(productRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.publishProduct(ProductId.generate()))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void archiveProduct_published_succeeds() {
        Product p = published();
        when(productRepository.findById(p.getId())).thenReturn(Optional.of(p));
        service.archiveProduct(p.getId());
        assertThat(p.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
    }

    @Test
    void unpublishProduct_published_back_to_draft() {
        Product p = published();
        when(productRepository.findById(p.getId())).thenReturn(Optional.of(p));
        service.unpublishProduct(p.getId(), UnpublishReason.DISCONTINUED);
        assertThat(p.getStatus()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    void updatePrice_publishes_event() {
        Product p = Product.createDraft(Sku.of("S"), "n", "d");
        when(productRepository.findById(any())).thenReturn(Optional.of(p));
        service.updatePrice(p.getId(), Money.of(new BigDecimal("100"), TWD));
        verify(eventPublisher).publishAll(anyList());
        assertThat(p.getListPrice()).isPresent();
    }

    @Test
    void updateDetails_renames_and_updates_description() {
        Product p = Product.createDraft(Sku.of("S"), "old", "old-desc");
        when(productRepository.findById(any())).thenReturn(Optional.of(p));
        service.updateDetails(new UpdateProductDetailsCommand(p.getId(), "new", "new-desc"));
        assertThat(p.getName()).isEqualTo("new");
        assertThat(p.getDescription()).isEqualTo("new-desc");
    }

    @Test
    void assignAndRemoveCategory_work() {
        Product p = Product.createDraft(Sku.of("S"), "n", "d");
        CategoryId cat = CategoryId.generate();
        when(productRepository.findById(any())).thenReturn(Optional.of(p));
        service.assignCategory(p.getId(), cat);
        assertThat(p.getCategoryIds()).contains(cat);
        service.removeCategory(p.getId(), cat);
        assertThat(p.getCategoryIds()).doesNotContain(cat);
    }

    private Product priced(String sku) {
        Product p = Product.createDraft(Sku.of(sku), "Name", "desc");
        p.updatePrice(Money.of(new BigDecimal("100"), TWD));
        p.pullDomainEvents();
        return p;
    }

    private Product published() {
        Product p = priced("S");
        p.publish();
        p.pullDomainEvents();
        return p;
    }
}
