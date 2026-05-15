package com.example.claudecodeclidemo.cart.domain.entity;

import com.example.claudecodeclidemo.cart.domain.event.CartCheckedOutEvent;
import com.example.claudecodeclidemo.cart.domain.exception.CartItemLimitExceededException;
import com.example.claudecodeclidemo.cart.domain.exception.CartItemNotFoundException;
import com.example.claudecodeclidemo.cart.domain.exception.EmptyCartException;
import com.example.claudecodeclidemo.cart.domain.vo.Money;
import com.example.claudecodeclidemo.cart.domain.vo.OrderId;
import com.example.claudecodeclidemo.cart.domain.vo.ProductId;
import com.example.claudecodeclidemo.cart.domain.vo.Quantity;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartTest {

    private static final Currency TWD = Currency.getInstance("TWD");
    private static final UserId USER_ID = new UserId(UUID.randomUUID());
    private static final ProductId PRODUCT_A = new ProductId(UUID.randomUUID());
    private static final ProductId PRODUCT_B = new ProductId(UUID.randomUUID());
    private static final Quantity QTY_1 = new Quantity(1);
    private static final Quantity QTY_2 = new Quantity(2);
    private static final Money PRICE = new Money(new BigDecimal("100"), TWD);

    // ── create ───────────────────────────────────────────────────────────

    @Test
    void create_withUserId_hasEmptyItems() {
        var cart = Cart.create(USER_ID);
        assertThat(cart.getId()).isNotNull();
        assertThat(cart.getUserId()).isEqualTo(USER_ID);
        assertThat(cart.getItems()).isEmpty();
    }

    // ── addItem ──────────────────────────────────────────────────────────

    @Test
    void addItem_newProduct_addsOneItem() {
        var cart = Cart.create(USER_ID);
        cart.addItem(PRODUCT_A, QTY_1, PRICE);
        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    void addItem_sameProductTwice_accumulatesQuantity_C2() {
        var cart = Cart.create(USER_ID);
        cart.addItem(PRODUCT_A, QTY_1, PRICE);
        cart.addItem(PRODUCT_A, QTY_2, PRICE);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(new Quantity(3));
    }

    @Test
    void addItem_fiftyFirstDistinctProduct_throwsCartItemLimitExceededException_C1() {
        var cart = Cart.create(USER_ID);
        for (int i = 0; i < 50; i++) {
            cart.addItem(new ProductId(UUID.randomUUID()), QTY_1, PRICE);
        }
        assertThatThrownBy(() -> cart.addItem(new ProductId(UUID.randomUUID()), QTY_1, PRICE))
                .isInstanceOf(CartItemLimitExceededException.class);
    }

    // ── updateItemQuantity ────────────────────────────────────────────────

    @Test
    void updateItemQuantity_existingProduct_updatesQuantity() {
        var cart = Cart.create(USER_ID);
        cart.addItem(PRODUCT_A, QTY_1, PRICE);
        cart.updateItemQuantity(PRODUCT_A, new Quantity(5));
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(new Quantity(5));
    }

    @Test
    void updateItemQuantity_productNotInCart_throwsCartItemNotFoundException() {
        var cart = Cart.create(USER_ID);
        cart.addItem(PRODUCT_A, QTY_1, PRICE);
        assertThatThrownBy(() -> cart.updateItemQuantity(PRODUCT_B, new Quantity(3)))
                .isInstanceOf(CartItemNotFoundException.class);
    }

    // ── removeItem ────────────────────────────────────────────────────────

    @Test
    void removeItem_existingProduct_removesIt() {
        var cart = Cart.create(USER_ID);
        cart.addItem(PRODUCT_A, QTY_1, PRICE);
        cart.addItem(PRODUCT_B, QTY_2, PRICE);
        cart.removeItem(PRODUCT_A);
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProductId()).isEqualTo(PRODUCT_B);
    }

    // ── checkout ─────────────────────────────────────────────────────────

    @Test
    void checkout_emptyCart_throwsEmptyCartException_C4() {
        var cart = Cart.create(USER_ID);
        var orderId = new OrderId(UUID.randomUUID());
        assertThatThrownBy(() -> cart.checkout(orderId))
                .isInstanceOf(EmptyCartException.class);
    }

    @Test
    void checkout_nonEmptyCart_clearsItems() {
        var cart = Cart.create(USER_ID);
        cart.addItem(PRODUCT_A, QTY_1, PRICE);
        cart.checkout(new OrderId(UUID.randomUUID()));
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void checkout_nonEmptyCart_registersCartCheckedOutEvent() {
        var cart = Cart.create(USER_ID);
        cart.addItem(PRODUCT_A, QTY_1, PRICE);
        var orderId = new OrderId(UUID.randomUUID());
        cart.checkout(orderId);

        assertThat(cart.getDomainEvents())
                .hasSize(1)
                .first().isInstanceOf(CartCheckedOutEvent.class);

        var event = (CartCheckedOutEvent) cart.getDomainEvents().get(0);
        assertThat(event.cartId()).isEqualTo(cart.getId());
        assertThat(event.userId()).isEqualTo(USER_ID);
        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.occurredAt()).isNotNull();
    }
}
