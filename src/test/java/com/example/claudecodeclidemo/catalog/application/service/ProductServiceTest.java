package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.in.CreateProductCommand;
import com.example.claudecodeclidemo.catalog.application.port.out.CategoryExistsPort;
import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.application.port.out.StockRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.entity.ProductStatus;
import com.example.claudecodeclidemo.catalog.domain.exception.CategoryNotFoundException;
import com.example.claudecodeclidemo.catalog.domain.exception.DuplicateSkuException;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import com.example.claudecodeclidemo.catalog.domain.event.ProductCreatedEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock StockRepository stockRepository;
    @Mock CategoryExistsPort categoryExistsPort;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks ProductService productService;

    private static final Currency TWD = Currency.getInstance("TWD");
    private static final Sku VALID_SKU = new Sku("PROD-001");
    private static final Money VALID_PRICE = new Money(new BigDecimal("100"), TWD);
    private static final CategoryId VALID_CATEGORY = new CategoryId(UUID.randomUUID());

    // ── Invariant P-4: SKU 唯一性（由 Service 層檢查）─────────────────

    @Test
    void SKU已存在時拋出DuplicateSkuException() {
        var command = new CreateProductCommand("測試商品", VALID_SKU, VALID_PRICE, VALID_CATEGORY);
        when(productRepository.findBySku(VALID_SKU)).thenReturn(Optional.of(mock(Product.class)));

        assertThatThrownBy(() -> productService.createProduct(command))
                .isInstanceOf(DuplicateSkuException.class);

        verify(productRepository, never()).save(any());
    }

    // ── Invariant P-6: CategoryId 必須存在（由 Service 層檢查）────────

    @Test
    void 分類不存在時拋出CategoryNotFoundException() {
        var command = new CreateProductCommand("測試商品", VALID_SKU, VALID_PRICE, VALID_CATEGORY);
        when(productRepository.findBySku(VALID_SKU)).thenReturn(Optional.empty());
        when(categoryExistsPort.exists(VALID_CATEGORY)).thenReturn(false);

        assertThatThrownBy(() -> productService.createProduct(command))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    // ── 正常建立商品 ──────────────────────────────────────────────────

    @Test
    void 合法命令成功建立商品並初始化庫存() {
        var command = new CreateProductCommand("測試商品", VALID_SKU, VALID_PRICE, VALID_CATEGORY);
        when(productRepository.findBySku(VALID_SKU)).thenReturn(Optional.empty());
        when(categoryExistsPort.exists(VALID_CATEGORY)).thenReturn(true);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var productId = productService.createProduct(command);

        assertThat(productId).isNotNull();
        verify(productRepository).save(any());
        verify(stockRepository).save(any());
        verify(eventPublisher, atLeastOnce()).publishEvent(isA(ProductCreatedEvent.class));
    }

    @Test
    void 建立商品後庫存初始為零() {
        var command = new CreateProductCommand("測試商品", VALID_SKU, VALID_PRICE, VALID_CATEGORY);
        when(productRepository.findBySku(VALID_SKU)).thenReturn(Optional.empty());
        when(categoryExistsPort.exists(VALID_CATEGORY)).thenReturn(true);
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(stockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        productService.createProduct(command);

        verify(stockRepository).save(argThat(stock ->
                stock.getQuantity() == 0 && stock.getReserved() == 0));
    }

    // ── 查詢商品 ──────────────────────────────────────────────────────

    @Test
    void 查詢不存在的商品拋出ProductNotFoundException() {
        var id = new ProductId(UUID.randomUUID());
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(id))
                .isInstanceOf(com.example.claudecodeclidemo.catalog.domain.exception.ProductNotFoundException.class);
    }

    @Test
    void 查詢存在的商品回傳商品() {
        var id = new ProductId(UUID.randomUUID());
        var product = Product.create("測試商品", VALID_SKU, VALID_PRICE, VALID_CATEGORY);
        when(productRepository.findById(any())).thenReturn(Optional.of(product));

        var result = productService.findById(id);

        assertThat(result.getName()).isEqualTo("測試商品");
        assertThat(result.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }
}
