package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductUseCase.CreateProductCommand;
import com.example.claudecodeclidemo.catalog.application.port.out.CategoryRepository;
import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.application.port.out.StockRepository;
import com.example.claudecodeclidemo.catalog.domain.exception.CategoryNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.exception.DuplicateSkuException;
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
import java.util.UUID;

import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateProductServiceTest {

    private static final Currency TWD = Currency.getInstance("TWD");
    private static final Sku VALID_SKU = new Sku("PROD-001");
    private static final Money VALID_PRICE = new Money(new BigDecimal("100.00"), TWD);
    private static final CategoryId CATEGORY_ID = new CategoryId(UUID.randomUUID());

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private CreateProductService createProductService;

    // ── 正常流程 ──────────────────────────────────────────────────────────

    @Test
    void createProduct_validCommand_returnsProductId() {
        when(productRepository.existsBySku(VALID_SKU)).thenReturn(false);
        when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(true);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreateProductCommand("Test Product", VALID_SKU, VALID_PRICE, CATEGORY_ID);
        var productId = createProductService.createProduct(command);

        assertThat(productId).isNotNull().isInstanceOf(ProductId.class);
    }

    @Test
    void createProduct_savesProductAndInitialStock() {
        when(productRepository.existsBySku(VALID_SKU)).thenReturn(false);
        when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(true);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreateProductCommand("Test Product", VALID_SKU, VALID_PRICE, CATEGORY_ID);
        createProductService.createProduct(command);

        verify(productRepository).save(any());
        verify(stockRepository).save(any());
    }

    @Test
    void createProduct_newProduct_statusIsActive() {
        when(productRepository.existsBySku(VALID_SKU)).thenReturn(false);
        when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(true);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var captor = ArgumentCaptor.forClass(com.example.claudecodeclidemo.catalog.domain.entity.Product.class);
        var command = new CreateProductCommand("Test Product", VALID_SKU, VALID_PRICE, CATEGORY_ID);
        createProductService.createProduct(command);

        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    // ── P-4: SKU 唯一性驗證 ──────────────────────────────────────────────

    @Test
    void createProduct_duplicateSku_throwsDuplicateSkuException() {
        when(productRepository.existsBySku(VALID_SKU)).thenReturn(true);

        var command = new CreateProductCommand("Test Product", VALID_SKU, VALID_PRICE, CATEGORY_ID);
        assertThatThrownBy(() -> createProductService.createProduct(command))
                .isInstanceOf(DuplicateSkuException.class);

        verify(productRepository, never()).save(any());
    }

    // ── P-6: 分類存在性驗證 ──────────────────────────────────────────────

    @Test
    void createProduct_categoryNotFound_throwsCategoryNotFoundException() {
        when(productRepository.existsBySku(VALID_SKU)).thenReturn(false);
        when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(false);

        var command = new CreateProductCommand("Test Product", VALID_SKU, VALID_PRICE, CATEGORY_ID);
        assertThatThrownBy(() -> createProductService.createProduct(command))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(productRepository, never()).save(any());
    }
}
