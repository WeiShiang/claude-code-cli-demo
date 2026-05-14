package com.example.claudecodeclidemo.catalog.adapter.out.persistence;

import com.example.claudecodeclidemo.catalog.domain.entity.Product;
import com.example.claudecodeclidemo.catalog.domain.entity.Stock;
import com.example.claudecodeclidemo.catalog.domain.vo.*;
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
class ProductPersistenceTest {

    @Autowired ProductRepositoryAdapter productRepo;
    @Autowired StockRepositoryAdapter stockRepo;
    @Autowired CategoryExistsAdapter categoryExists;
    @Autowired CategoryJpaRepository categoryJpaRepo;

    private static final Currency TWD = Currency.getInstance("TWD");

    @Test
    void 商品儲存後可以用ID查詢() {
        var product = Product.create(
                "測試商品", new Sku("PROD-001"),
                new Money(new BigDecimal("100"), TWD),
                new CategoryId(UUID.randomUUID())
        );
        var saved = productRepo.save(product);

        var found = productRepo.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("測試商品");
        assertThat(found.get().getSku().value()).isEqualTo("PROD-001");
    }

    @Test
    void 商品儲存後可以用SKU查詢() {
        var sku = new Sku("PROD-002");
        var product = Product.create(
                "另一商品", sku,
                new Money(new BigDecimal("200"), TWD),
                new CategoryId(UUID.randomUUID())
        );
        productRepo.save(product);

        var found = productRepo.findBySku(sku);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("另一商品");
    }

    @Test
    void 庫存儲存後可以用ProductId查詢() {
        var productId = ProductId.generate();
        var stock = Stock.create(productId, new Sku("PROD-003"), 10);
        stockRepo.save(stock);

        var found = stockRepo.findByProductId(productId);

        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualTo(10);
        assertThat(found.get().getReserved()).isEqualTo(0);
    }

    @Test
    void 庫存更新後持久化新數值() {
        var productId = ProductId.generate();
        var stock = Stock.create(productId, new Sku("PROD-004"), 10);
        stockRepo.save(stock);
        stock.reserve(3);
        stockRepo.save(stock);

        var found = stockRepo.findByProductId(productId);

        assertThat(found).isPresent();
        assertThat(found.get().getReserved()).isEqualTo(3);
    }

    @Test
    void 分類存在時CategoryExistsAdapter回傳true() {
        var categoryId = UUID.randomUUID();
        categoryJpaRepo.save(new CategoryJpaEntity(categoryId, "電子產品"));

        assertThat(categoryExists.exists(new CategoryId(categoryId))).isTrue();
    }

    @Test
    void 分類不存在時CategoryExistsAdapter回傳false() {
        assertThat(categoryExists.exists(new CategoryId(UUID.randomUUID()))).isFalse();
    }
}
