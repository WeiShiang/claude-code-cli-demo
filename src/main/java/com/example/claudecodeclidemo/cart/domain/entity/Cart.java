package com.example.claudecodeclidemo.cart.domain.entity;

import com.example.claudecodeclidemo.cart.domain.event.CartCheckedOutEvent;
import com.example.claudecodeclidemo.cart.domain.event.CartItemSnapshot;
import com.example.claudecodeclidemo.cart.domain.exception.CartItemLimitExceededException;
import com.example.claudecodeclidemo.cart.domain.exception.CartItemNotFoundException;
import com.example.claudecodeclidemo.cart.domain.exception.EmptyCartException;
import com.example.claudecodeclidemo.cart.domain.vo.CartId;
import com.example.claudecodeclidemo.cart.domain.vo.Money;
import com.example.claudecodeclidemo.cart.domain.vo.OrderId;
import com.example.claudecodeclidemo.cart.domain.vo.ProductId;
import com.example.claudecodeclidemo.cart.domain.vo.Quantity;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cart extends AggregateRoot {

    private static final int MAX_ITEMS = 50;

    private final CartId id;
    private final UserId userId;
    private final List<CartItem> items;
    private Instant updatedAt;

    private Cart(CartId id, UserId userId) {
        this.id = id;
        this.userId = userId;
        this.items = new ArrayList<>();
        this.updatedAt = Instant.now();
    }

    public static Cart create(UserId userId) {
        return new Cart(CartId.generate(), userId);
    }

    public static Cart reconstitute(CartId id, UserId userId, List<CartItem> items, Instant updatedAt) {
        var cart = new Cart(id, userId);
        cart.items.addAll(items);
        cart.updatedAt = updatedAt;
        return cart;
    }

    public void addItem(ProductId productId, Quantity quantity, Money unitPrice) {
        var existing = findItem(productId);
        if (existing != null) {
            existing.addQuantity(quantity);
            this.updatedAt = Instant.now();
            return;
        }
        if (items.size() >= MAX_ITEMS) throw new CartItemLimitExceededException();
        items.add(CartItem.create(productId, quantity, unitPrice));
        this.updatedAt = Instant.now();
    }

    public void updateItemQuantity(ProductId productId, Quantity quantity) {
        var item = findItem(productId);
        if (item == null) throw new CartItemNotFoundException(productId);
        item.updateQuantity(quantity);
        this.updatedAt = Instant.now();
    }

    public void removeItem(ProductId productId) {
        items.removeIf(i -> i.getProductId().equals(productId));
        this.updatedAt = Instant.now();
    }

    public void checkout(OrderId orderId) {
        if (items.isEmpty()) throw new EmptyCartException();
        var snapshots = items.stream()
                .map(i -> new CartItemSnapshot(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                .toList();
        registerEvent(new CartCheckedOutEvent(this.id, this.userId, orderId, snapshots, Instant.now()));
        items.clear();
        this.updatedAt = Instant.now();
    }

    private CartItem findItem(ProductId productId) {
        return items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    public CartId getId() { return id; }
    public UserId getUserId() { return userId; }
    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }
    public Instant getUpdatedAt() { return updatedAt; }
}
