package com.example.claudecodeclidemo.catalog.domain.entity;

import com.example.claudecodeclidemo.catalog.domain.event.ProductCreatedEvent;
import com.example.claudecodeclidemo.catalog.domain.event.ProductPriceChangedEvent;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductNameException;
import com.example.claudecodeclidemo.catalog.domain.exception.InvalidProductStateTransitionException;
import com.example.claudecodeclidemo.catalog.domain.vo.CategoryId;
import com.example.claudecodeclidemo.catalog.domain.vo.Money;
import com.example.claudecodeclidemo.catalog.domain.vo.ProductStatus;
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
    private static final Money VALID_PRICE = new Money(new BigDecimal("100.00"), TWD);
    private static final CategoryId CATEGORY_ID = new CategoryId(UUID.randomUUID());

    // ── P-1: 商品名稱不可為空白或空字串 ───────────────────────────────────

    @Test
    void create_withBlankName_throwsInvalidProductNameException() {
        assertThatThrownBy(() -> Product.create("  ", VALID_SKU, VALID_PRICE, CATEGORY_ID))
                .isInstanceOf(InvalidProductNameException.class);
    }

    @Test
    void create_withEmptyName_throwsInvalidProductNameException() {
        assertThatThrownBy(() -> Product.create("", VALID_SKU, VALID_PRICE, CATEGORY_ID))
                .isInstanceOf(InvalidProductNameException.class);
    }

    // ── P-5: DISCONTINUED 商品不可被重新上架 ─────────────────────────────

    @Test
    void activate_discontinuedProduct_throwsInvalidProductStateTransitionException() {
        var product = aProduct();
        product.discontinue();
        assertThatThrownBy(product::activate)
                .isInstanceOf(InvalidProductStateTransitionException.class);
    }

    // ── 狀態轉換合法路徑 ──────────────────────────────────────────────────

    @Test
    void create_defaultStatus_isActive() {
        var product = aProduct();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void deactivate_activeProduct_changesStatusToInactive() {
        var product = aProduct();
        product.deactivate();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    void activate_inactiveProduct_changesStatusToActive() {
        var product = aProduct();
        product.deactivate();
        product.activate();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void discontinue_activeProduct_changesStatusToDiscontinued() {
        var product = aProduct();
        product.discontinue();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
    }

    @Test
    void discontinue_inactiveProduct_changesStatusToDiscontinued() {
        var product = aProduct();
        product.deactivate();
        product.discontinue();
        assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
    }

    // ── 定價變更 ──────────────────────────────────────────────────────────

    @Test
    void changePrice_validPrice_updatesPrice() {
        var product = aProduct();
        var newPrice = new Money(new BigDecimal("200.00"), TWD);
        product.changePrice(newPrice);
        assertThat(product.getPrice()).isEqualTo(newPrice);
    }

    @Test
    void changePrice_publishesPriceChangedEvent() {
        var product = aProduct();
        var newPrice = new Money(new BigDecimal("200.00"), TWD);
        product.changePrice(newPrice);
        assertThat(product.getDomainEvents())
                .hasAtLeastOneElementOfType(ProductPriceChangedEvent.class);
    }

    // ── Domain Event 發布 ─────────────────────────────────────────────────

    @Test
    void create_publishesProductCreatedEvent() {
        var product = aProduct();
        assertThat(product.getDomainEvents())
                .hasSize(1)
                .first().isInstanceOf(ProductCreatedEvent.class);
    }

    @Test
    void productCreatedEvent_containsCorrectData() {
        var product = aProduct();
        var event = (ProductCreatedEvent) product.getDomainEvents().get(0);
        assertThat(event.productId()).isEqualTo(product.getId());
        assertThat(event.sku()).isEqualTo(VALID_SKU);
        assertThat(event.price()).isEqualTo(VALID_PRICE);
    }

    // ── 屬性存取 ──────────────────────────────────────────────────────────

    @Test
    void create_setsAllFields() {
        var product = Product.create("Test Product", VALID_SKU, VALID_PRICE, CATEGORY_ID);
        assertThat(product.getId()).isNotNull();
        assertThat(product.getName()).isEqualTo("Test Product");
        assertThat(product.getSku()).isEqualTo(VALID_SKU);
        assertThat(product.getPrice()).isEqualTo(VALID_PRICE);
        assertThat(product.getCategoryId()).isEqualTo(CATEGORY_ID);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Product aProduct() {
        return Product.create("Test Product", VALID_SKU, VALID_PRICE, CATEGORY_ID);
    }
}
