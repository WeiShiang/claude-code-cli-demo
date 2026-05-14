package com.example.claudecodeclidemo.catalog.domain.entity;

import com.example.claudecodeclidemo.catalog.domain.event.ProductCreatedEvent;
import com.example.claudecodeclidemo.catalog.domain.event.ProductPriceChangedEvent;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductNameException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductStateTransitionException;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.InvalidPriceException;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.Sku;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private static final Currency TWD = Currency.getInstance("TWD");
    private static final Sku VALID_SKU = new Sku("PROD-001");
    private static final Money VALID_PRICE = new Money(new BigDecimal("100"), TWD);
    private static final CategoryId VALID_CATEGORY = new CategoryId(UUID.randomUUID());

    // ── Invariant P-1: 商品名稱不可為空 ──────────────────────────────

    @Test
    void 空白名稱建立商品拋出InvalidProductNameException() {
        assertThatThrownBy(() -> Product.create("  ", VALID_SKU, VALID_PRICE, VALID_CATEGORY))
                .isInstanceOf(InvalidProductNameException.class);
    }

    @Test
    void 空字串名稱建立商品拋出InvalidProductNameException() {
        assertThatThrownBy(() -> Product.create("", VALID_SKU, VALID_PRICE, VALID_CATEGORY))
                .isInstanceOf(InvalidProductNameException.class);
    }

    @Test
    void null名稱建立商品拋出InvalidProductNameException() {
        assertThatThrownBy(() -> Product.create(null, VALID_SKU, VALID_PRICE, VALID_CATEGORY))
                .isInstanceOf(InvalidProductNameException.class);
    }

    // ── Invariant P-2: 定價 ≥ 0（由 Money 守衛）─────────────────────

    @Test
    void 負數定價建立Money時拋出InvalidPriceException() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-1"), TWD))
                .isInstanceOf(InvalidPriceException.class);
    }

    // ── 正常建立 ──────────────────────────────────────────────────────

    @Test
    void 合法參數可建立商品() {
        var product = Product.create("測試商品", VALID_SKU, VALID_PRICE, VALID_CATEGORY);
        assertThat(product.getId()).isNotNull();
        assertThat(product.getName()).isEqualTo("測試商品");
        assertThat(product.getSku()).isEqualTo(VALID_SKU);
        assertThat(product.getPrice()).isEqualTo(VALID_PRICE);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    // ── Invariant P-5: DISCONTINUED 不可重新上架 ─────────────────────

    @Test
    void DISCONTINUED商品重新上架拋出InvalidProductStateTransitionException() {
        var product = Product.create("測試商品", VALID_SKU, VALID_PRICE, VALID_CATEGORY);
        product.discontinue();
        assertThatThrownBy(product::activate)
                .isInstanceOf(InvalidProductStateTransitionException.class);
    }

    @Test
    void ACTIVE商品可以下架() {
        var product = Product.create("測試商品", VALID_SKU, VALID_PRICE, VALID_CATEGORY);
        product.deactivate();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    void INACTIVE商品可以重新上架() {
        var product = Product.create("測試商品", VALID_SKU, VALID_PRICE, VALID_CATEGORY);
        product.deactivate();
        product.activate();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void ACTIVE商品可以停售() {
        var product = Product.create("測試商品", VALID_SKU, VALID_PRICE, VALID_CATEGORY);
        product.discontinue();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
    }

    // ── Domain Events ─────────────────────────────────────────────────

    @Test
    void 建立商品時發布ProductCreatedEvent() {
        var product = Product.create("測試商品", VALID_SKU, VALID_PRICE, VALID_CATEGORY);
        assertThat(product.getDomainEvents())
                .hasSize(1)
                .first().isInstanceOf(ProductCreatedEvent.class);

        var event = (ProductCreatedEvent) product.getDomainEvents().get(0);
        assertThat(event.productId()).isEqualTo(product.getId());
        assertThat(event.sku()).isEqualTo(VALID_SKU);
        assertThat(event.price()).isEqualTo(VALID_PRICE);
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    void 更新定價時發布ProductPriceChangedEvent() {
        var product = Product.create("測試商品", VALID_SKU, VALID_PRICE, VALID_CATEGORY);
        product.clearDomainEvents();

        var newPrice = new Money(new BigDecimal("200"), TWD);
        product.changePrice(newPrice);

        assertThat(product.getDomainEvents())
                .hasSize(1)
                .first().isInstanceOf(ProductPriceChangedEvent.class);

        var event = (ProductPriceChangedEvent) product.getDomainEvents().get(0);
        assertThat(event.oldPrice()).isEqualTo(VALID_PRICE);
        assertThat(event.newPrice()).isEqualTo(newPrice);
    }
}
