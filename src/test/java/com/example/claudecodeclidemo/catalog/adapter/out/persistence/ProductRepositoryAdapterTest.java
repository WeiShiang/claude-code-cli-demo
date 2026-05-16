package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.domain.entity.Category;
import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.entity.Stock;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductId;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductStatus;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductRepositoryAdapterTest {

    private static final Currency TWD = Currency.getInstance("TWD");
    private static final CategoryId CATEGORY_ID = new CategoryId(UUID.fromString("11111111-0000-0000-0000-000000000001"));
    private static final Sku SKU = new Sku("PHONE-001");
    private static final Money PRICE = new Money(new BigDecimal("999.00"), TWD);

    @Autowired private ProductRepositoryAdapter productAdapter;
    @Autowired private StockRepositoryAdapter stockAdapter;
    @Autowired private CategoryRepositoryAdapter categoryAdapter;

    // ── ProductRepositoryAdapter ─────────────────────────────────────────

    @Test
    void save_and_findById_roundTrip() {
        var product = Product.create("Test Phone", SKU, PRICE, CATEGORY_ID);
        productAdapter.save(product);

        var found = productAdapter.findById(product.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Phone");
        assertThat(found.get().getSku()).isEqualTo(SKU);
        assertThat(found.get().getPrice()).isEqualTo(PRICE);
        assertThat(found.get().getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void findById_notExist_returnsEmpty() {
        assertThat(productAdapter.findById(new ProductId(UUID.randomUUID()))).isEmpty();
    }

    @Test
    void existsBySku_existingSku_returnsTrue() {
        productAdapter.save(Product.create("Test Phone", SKU, PRICE, CATEGORY_ID));
        assertThat(productAdapter.existsBySku(SKU)).isTrue();
    }

    @Test
    void existsBySku_unknownSku_returnsFalse() {
        assertThat(productAdapter.existsBySku(new Sku("UNKN-999"))).isFalse();
    }

    // ── StockRepositoryAdapter ───────────────────────────────────────────

    @Test
    void stock_save_and_findByProductId_roundTrip() {
        var product = Product.create("Test Phone", SKU, PRICE, CATEGORY_ID);
        productAdapter.save(product);

        stockAdapter.save(Stock.of(product.getId(), 10));

        var found = stockAdapter.findByProductId(product.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualTo(10);
        assertThat(found.get().getReserved()).isEqualTo(0);
    }

    // ── CategoryRepositoryAdapter ────────────────────────────────────────

    @Test
    void categoryExistsById_savedCategory_returnsTrue() {
        var category = Category.create("Electronics");
        categoryAdapter.save(category);
        assertThat(categoryAdapter.existsById(category.getId())).isTrue();
    }

    @Test
    void categoryExistsById_unknownId_returnsFalse() {
        assertThat(categoryAdapter.existsById(new CategoryId(UUID.randomUUID()))).isFalse();
    }
}
