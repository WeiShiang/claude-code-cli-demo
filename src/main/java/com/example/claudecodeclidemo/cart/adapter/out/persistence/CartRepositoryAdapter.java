package com.example.claudecodeclidemo.cart.adapter.out.persistence;

import com.example.claudecodeclidemo.cart.application.port.out.CartRepository;
import com.example.claudecodeclidemo.cart.domain.entity.Cart;
import com.example.claudecodeclidemo.cart.domain.entity.CartItem;
import com.example.claudecodeclidemo.cart.domain.vo.CartId;
import com.example.claudecodeclidemo.cart.domain.vo.CartItemId;
import com.example.claudecodeclidemo.cart.domain.vo.Money;
import com.example.claudecodeclidemo.cart.domain.vo.ProductId;
import com.example.claudecodeclidemo.cart.domain.vo.Quantity;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Repository
class CartRepositoryAdapter implements CartRepository {

    private final CartJpaRepository jpa;

    CartRepositoryAdapter(CartJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Cart> findByUserId(UserId userId) {
        return jpa.findByUserId(userId.value()).map(this::toDomain);
    }

    @Override
    public Cart save(Cart cart) {
        var entity = toJpa(cart);
        var saved = jpa.save(entity);
        return toDomain(saved);
    }

    private CartJpaEntity toJpa(Cart cart) {
        var entity = new CartJpaEntity(cart.getId().value(), cart.getUserId().value(), cart.getUpdatedAt());
        var itemEntities = cart.getItems().stream()
                .map(i -> new CartItemJpaEntity(
                        i.getId().value(), entity,
                        i.getProductId().value(),
                        i.getQuantity().value(),
                        i.getUnitPrice().amount(),
                        i.getUnitPrice().currency().getCurrencyCode()))
                .toList();
        entity.items.addAll(itemEntities);
        return entity;
    }

    private Cart toDomain(CartJpaEntity e) {
        List<CartItem> items = e.items.stream()
                .map(i -> CartItem.reconstitute(
                        new CartItemId(i.id),
                        new ProductId(i.productId),
                        new Quantity(i.quantity),
                        new Money(i.unitPriceAmount, Currency.getInstance(i.unitPriceCurrency))))
                .toList();
        return Cart.reconstitute(
                new CartId(e.id),
                new UserId(e.userId),
                items,
                e.updatedAt);
    }
}
