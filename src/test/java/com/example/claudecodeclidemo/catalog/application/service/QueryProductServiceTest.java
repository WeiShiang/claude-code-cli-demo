package com.example.claudecodeclidemo.catalog.application.service;

import com.example.claudecodeclidemo.catalog.application.port.out.ProductRepository;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryProductServiceTest {

    private static final Currency TWD = Currency.getInstance("TWD");
    private static final ProductId PRODUCT_ID = new ProductId(UUID.randomUUID());
    private static final Sku VALID_SKU = new Sku("PROD-001");
    private static final Money VALID_PRICE = new Money(new BigDecimal("100.00"), TWD);
    private static final CategoryId CATEGORY_ID = new CategoryId(UUID.randomUUID());

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private QueryProductService queryProductService;

    @Test
    void findById_productExists_returnsProduct() {
        Product product = Product.create("Test Product", VALID_SKU, VALID_PRICE, CATEGORY_ID);
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        Optional<Product> result = queryProductService.findById(PRODUCT_ID);

        assertThat(result).isPresent();
        verify(productRepository).findById(PRODUCT_ID);
    }

    @Test
    void findById_productNotFound_returnsEmpty() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        Optional<Product> result = queryProductService.findById(PRODUCT_ID);

        assertThat(result).isEmpty();
        verify(productRepository).findById(PRODUCT_ID);
    }
}
