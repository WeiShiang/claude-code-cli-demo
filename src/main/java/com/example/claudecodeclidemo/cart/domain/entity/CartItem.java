package com.example.claudecodeclidemo.cart.domain.entity;

import com.example.claudecodeclidemo.cart.domain.vo.CartItemId;
import com.example.claudecodeclidemo.cart.domain.vo.Money;
import com.example.claudecodeclidemo.cart.domain.vo.ProductId;
import com.example.claudecodeclidemo.cart.domain.vo.Quantity;

public class CartItem {
    private final CartItemId id;
    private final ProductId productId;
    private Quantity quantity;
    private final Money unitPrice;

    private CartItem(CartItemId id, ProductId productId, Quantity quantity, Money unitPrice) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public static CartItem create(ProductId productId, Quantity quantity, Money unitPrice) {
        return new CartItem(CartItemId.generate(), productId, quantity, unitPrice);
    }

    public static CartItem reconstitute(CartItemId id, ProductId productId, Quantity quantity, Money unitPrice) {
        return new CartItem(id, productId, quantity, unitPrice);
    }

    public void addQuantity(Quantity additional) {
        this.quantity = this.quantity.add(additional);
    }

    public void updateQuantity(Quantity newQuantity) {
        this.quantity = newQuantity;
    }

    public CartItemId getId() { return id; }
    public ProductId getProductId() { return productId; }
    public Quantity getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }
}
