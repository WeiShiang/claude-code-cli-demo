# Domain Events — Shopping Mall

> **領域事件（Domain Event）** 描述 BC 內發生的重要狀態變更，以**過去式**命名。  
> MVP 使用 Spring `ApplicationEventPublisher` 同步廣播；v2+ 升級 Kafka 時只需替換 adapter。

---

## 事件命名慣例

- **格式**：`<Entity><動詞過去式>Event`（例：`OrderCreatedEvent`）
- **發布位置**：Aggregate Root 的方法中，或 Use Case 完成後
- **Package**：`com.example.claudecodeclidemo.<bc>.domain.event`

---

## Catalog BC

### `ProductCreatedEvent`
| 欄位 | 型別 | 說明 |
|---|---|---|
| productId | `ProductId` | 新建商品 ID |
| sku | `Sku` | SKU 編碼 |
| price | `Money` | 初始定價 |
| occurredAt | `Instant` | 事件發生時間 |

**觸發**：`CreateProductUseCase` 成功建立 Product  
**消費者**（目前）：無（預留 Search/Recommendation v2）

---

### `ProductPriceChangedEvent`
| 欄位 | 型別 | 說明 |
|---|---|---|
| productId | `ProductId` | |
| oldPrice | `Money` | 原定價 |
| newPrice | `Money` | 新定價 |
| occurredAt | `Instant` | |

**觸發**：`UpdateProductUseCase` 修改定價  
**消費者**：Cart BC（更新購物車中的單價快照，通知使用者）— v2 實作

---

### `StockDepletedEvent`
| 欄位 | 型別 | 說明 |
|---|---|---|
| productId | `ProductId` | |
| sku | `Sku` | |
| occurredAt | `Instant` | |

**觸發**：`deduct()` 後可用庫存降為 0  
**消費者**：Catalog（自動下架 / 發通知）— v2 實作

---

## Cart BC

### `CartCheckedOutEvent`
| 欄位 | 型別 | 說明 |
|---|---|---|
| cartId | `CartId` | |
| userId | `UserId` | |
| orderId | `OrderId` | 建立的訂單 ID |
| items | `List<CartItemSnapshot>` | 結帳品項快照 |
| occurredAt | `Instant` | |

**觸發**：`CheckoutUseCase` 成功建立訂單並清空購物車  
**消費者**：無（Analytics v2）

---

## Order BC

### `OrderCreatedEvent`  ⭐ 核心事件
| 欄位 | 型別 | 說明 |
|---|---|---|
| orderId | `OrderId` | |
| userId | `UserId` | |
| lines | `List<OrderLineSnapshot>` | 訂單明細快照 |
| totalAmount | `Money` | |
| occurredAt | `Instant` | |

**觸發**：`CreateOrderUseCase` 完成訂單建立  
**消費者**：
- **Payment BC** — 自動發起付款（同步 Port 呼叫，非事件驅動；v2 可改為事件）
- 稽核日誌（Audit Log）

---

### `OrderPaidEvent`  ⭐ 核心事件
| 欄位 | 型別 | 說明 |
|---|---|---|
| orderId | `OrderId` | |
| userId | `UserId` | |
| paidAmount | `Money` | |
| paymentId | `PaymentId` | 對應付款單 |
| occurredAt | `Instant` | |

**觸發**：Order 狀態從 `CREATED` 轉 `PAID`（在 `PaymentSucceededEvent` handler 內）  
**消費者**：
- **Catalog BC** — `StockReservationPort.deduct()` 正式扣除庫存
- Fulfillment BC（v2）— 觸發出貨流程

---

### `OrderCancelledEvent`
| 欄位 | 型別 | 說明 |
|---|---|---|
| orderId | `OrderId` | |
| userId | `UserId` | |
| reason | `CancellationReason` | USER_REQUEST / PAYMENT_FAILED / SYSTEM |
| occurredAt | `Instant` | |

**觸發**：Order 狀態轉 `CANCELLED`  
**消費者**：
- **Catalog BC** — `StockReservationPort.release()` 釋放保留庫存
- Notification BC（v2）— 發送取消通知給使用者

---

## Payment BC

### `PaymentSucceededEvent`  ⭐ 核心事件
| 欄位 | 型別 | 說明 |
|---|---|---|
| paymentId | `PaymentId` | |
| orderId | `OrderId` | |
| amount | `Money` | |
| paidAt | `Instant` | |

**觸發**：Payment 狀態轉 `SUCCEEDED`（金流商回傳成功）  
**消費者**：
- **Order BC** — 將訂單狀態更新為 `PAID`

---

### `PaymentFailedEvent`  ⭐ 核心事件
| 欄位 | 型別 | 說明 |
|---|---|---|
| paymentId | `PaymentId` | |
| orderId | `OrderId` | |
| failureCode | `String` | 金流商錯誤碼 |
| occurredAt | `Instant` | |

**觸發**：Payment 狀態轉 `FAILED`  
**消費者**：
- **Order BC** — 將訂單狀態更新為 `CANCELLED`，觸發庫存釋放

---

## 事件流全覽

> **架構說明**：Port 呼叫（`StockReservationPort`、`OrderCheckoutPort`、`PaymentPort`）  
> 發生在 **Application Service** 層，而非 Domain Entity 內部。  
> Domain Entity 只負責業務邏輯與發出 Domain Event；副作用由 Application Service 協調。

```
使用者結帳
    │
    ▼
CheckoutService.checkout()          ← Application Service（協調者）
    ├─► CatalogQueryPort.check()    ← Port Out 呼叫（C-5 商品可用性驗證）
    ├─► cart.checkout()             ← Domain 操作（C-4 空車驗證 + 發出 CartCheckedOutEvent）
    ├─► OrderCheckoutPort.createOrder()  ← Port Out 呼叫
    └─► [CartCheckedOutEvent]

CreateOrderService.createOrder()    ← Application Service（協調者）
    ├─► StockReservationPort.reserve()  ← Port Out 呼叫（先保留庫存）
    ├─► Order.create()              ← Domain 操作（發出 OrderCreatedEvent）
    ├─► OrderRepository.save()      ← Port Out 呼叫
    ├─► PaymentPort.initiate()      ← Port Out 呼叫（發起付款）
    └─► [OrderCreatedEvent]

Payment.process()                   ← Domain 操作
    │
    ├── 成功 → [PaymentSucceededEvent]
    └── 失敗 → [PaymentFailedEvent]

PaymentEventHandler.on(PaymentSucceededEvent)   ← Event Listener
    ├─► order.markAsPaid()          ← Domain 操作（發出 OrderPaidEvent）
    ├─► StockReservationPort.deduct()  ← Port Out 呼叫
    └─► [OrderPaidEvent]

PaymentEventHandler.on(PaymentFailedEvent)      ← Event Listener
    ├─► order.cancel()              ← Domain 操作（發出 OrderCancelledEvent）
    ├─► StockReservationPort.release()  ← Port Out 呼叫
    └─► [OrderCancelledEvent]
```

---

## Spring 實作慣例（MVP）

```java
// 發布事件（在 Use Case 或 Aggregate）
applicationEventPublisher.publishEvent(new OrderCreatedEvent(order.getId(), ...));

// 訂閱事件（在 Event Handler / Application Service）
@EventListener
@Transactional
public void on(PaymentSucceededEvent event) {
    orderService.markAsPaid(event.getOrderId(), event.getPaymentId());
}
```

**注意事項**：
- MVP 使用同步事件，`@EventListener` 與發布者在**同一個 Transaction** 內執行
- 若需要事件失敗不影響主流程，改用 `@TransactionalEventListener(phase = AFTER_COMMIT)`
- v2 升 Kafka 時，只需替換 `ApplicationEventPublisher` 的 adapter 實作，Use Case 不改

---
