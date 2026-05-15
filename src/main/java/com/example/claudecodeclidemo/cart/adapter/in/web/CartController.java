package com.example.claudecodeclidemo.cart.adapter.in.web;

import com.example.claudecodeclidemo.cart.application.port.in.AddItemCommand;
import com.example.claudecodeclidemo.cart.application.port.in.AddItemUseCase;
import com.example.claudecodeclidemo.cart.application.port.in.CheckoutCommand;
import com.example.claudecodeclidemo.cart.application.port.in.CheckoutUseCase;
import com.example.claudecodeclidemo.cart.application.port.in.RemoveItemCommand;
import com.example.claudecodeclidemo.cart.application.port.in.RemoveItemUseCase;
import com.example.claudecodeclidemo.cart.application.port.in.UpdateItemCommand;
import com.example.claudecodeclidemo.cart.application.port.in.UpdateItemUseCase;
import com.example.claudecodeclidemo.cart.domain.vo.ProductId;
import com.example.claudecodeclidemo.cart.domain.vo.Quantity;
import com.example.claudecodeclidemo.cart.domain.vo.UserId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/carts/{userId}")
class CartController {

    private final AddItemUseCase addItem;
    private final UpdateItemUseCase updateItem;
    private final RemoveItemUseCase removeItem;
    private final CheckoutUseCase checkout;

    CartController(AddItemUseCase addItem, UpdateItemUseCase updateItem,
                   RemoveItemUseCase removeItem, CheckoutUseCase checkout) {
        this.addItem = addItem;
        this.updateItem = updateItem;
        this.removeItem = removeItem;
        this.checkout = checkout;
    }

    @PostMapping("/items")
    ResponseEntity<Void> addItem(@PathVariable UUID userId,
                                 @RequestBody AddItemRequest request) {
        addItem.addItem(new AddItemCommand(
                new UserId(userId),
                new ProductId(request.productId()),
                new Quantity(request.quantity())));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{productId}")
    ResponseEntity<Void> updateItem(@PathVariable UUID userId,
                                    @PathVariable UUID productId,
                                    @RequestBody UpdateItemRequest request) {
        updateItem.updateItem(new UpdateItemCommand(
                new UserId(userId),
                new ProductId(productId),
                new Quantity(request.quantity())));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items/{productId}")
    ResponseEntity<Void> removeItem(@PathVariable UUID userId,
                                    @PathVariable UUID productId) {
        removeItem.removeItem(new RemoveItemCommand(
                new UserId(userId),
                new ProductId(productId)));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    ResponseEntity<CartResponse> checkout(@PathVariable UUID userId) {
        var orderId = checkout.checkout(new CheckoutCommand(new UserId(userId)));
        return ResponseEntity.ok(new CartResponse(orderId.value()));
    }
}
