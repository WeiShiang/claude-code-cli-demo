package com.example.claudecodeclidemo.cart.adapter.out.persistence;

import com.example.claudecodeclidemo.cart.domain.entity.Cart;
import com.example.claudecodeclidemo.cart.domain.vo.Money;
import com.example.claudecodeclidemo.cart.domain.vo.OrderId;
import com.example.claudecodeclidemo.cart.domain.vo.ProductId;
import com.example.claudecodeclidemo.cart.domain.vo.Quantity;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;
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
class CartPersistenceTest {

    @Autowired CartRepositoryAdapter cartRepo;

    private static final Currency TWD = Currency.getInstance("TWD");
    private static final Money PRICE = new Money(new BigDecimal("100"), TWD);

    @Test
    void save_newCart_canBeFoundByUserId() {
        var userId = new UserId(UUID.randomUUID());
        var cart = Cart.create(userId);
        cartRepo.save(cart);

        var found = cartRepo.findByUserId(userId);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId);
        assertThat(found.get().getItems()).isEmpty();
    }

    @Test
    void save_cartWithItems_persistsItemsCorrectly() {
        var userId = new UserId(UUID.randomUUID());
        var productId = new ProductId(UUID.randomUUID());
        var cart = Cart.create(userId);
        cart.addItem(productId, new Quantity(3), PRICE);
        cartRepo.save(cart);

        var found = cartRepo.findByUserId(userId).orElseThrow();

        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getProductId()).isEqualTo(productId);
        assertThat(found.getItems().get(0).getQuantity()).isEqualTo(new Quantity(3));
    }

    @Test
    void save_updatedCart_replacesItems() {
        var userId = new UserId(UUID.randomUUID());
        var productA = new ProductId(UUID.randomUUID());
        var productB = new ProductId(UUID.randomUUID());
        var cart = Cart.create(userId);
        cart.addItem(productA, new Quantity(1), PRICE);
        cartRepo.save(cart);

        var reloaded = cartRepo.findByUserId(userId).orElseThrow();
        reloaded.addItem(productB, new Quantity(2), PRICE);
        cartRepo.save(reloaded);

        var found = cartRepo.findByUserId(userId).orElseThrow();
        assertThat(found.getItems()).hasSize(2);
    }

    @Test
    void save_cartAfterCheckout_hasNoItems() {
        var userId = new UserId(UUID.randomUUID());
        var productId = new ProductId(UUID.randomUUID());
        var cart = Cart.create(userId);
        cart.addItem(productId, new Quantity(1), PRICE);
        cartRepo.save(cart);

        var reloaded = cartRepo.findByUserId(userId).orElseThrow();
        reloaded.checkout(new OrderId(UUID.randomUUID()));
        cartRepo.save(reloaded);

        var found = cartRepo.findByUserId(userId).orElseThrow();
        assertThat(found.getItems()).isEmpty();
    }
}
