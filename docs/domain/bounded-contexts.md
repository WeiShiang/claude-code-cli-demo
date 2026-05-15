# Bounded Contexts — Domain Reference

> 本文件為完整的 **Context Map**，描述各 BC 的職責、邊界、領域物件與上下游整合策略。  
> 原始架構規範見 `docs/bounded-contexts.md`；此文件聚焦 DDD 語意與設計決策。

---

## Context Map 全覽

```
        ┌──────────────────────────────────────────────────────────┐
        │                    Shopping Mall                         │
        │                                                          │
        │   ┌──────────┐  OHS/查價查庫  ┌──────────┐              │
        │   │ Catalog  │◄──────────────│   Cart   │              │
        │   │(Core)    │               │(Support) │              │
        │   └────┬─────┘               └────┬─────┘              │
        │        │ C/S                      │ C/S 結帳            │
        │        │ 庫存保留/扣除             ▼                     │
        │        │                    ┌──────────┐               │
        │        └───────────────────►│  Order   │               │
        │                             │ (Core)   │               │
        │                             └────┬─────┘               │
        │                                  │ Partnership          │
        │                                  │ 發起付款             │
        │                                  ▼                     │
        │                             ┌──────────┐               │
        │                             │ Payment  │               │
        │                             │ (Core)   │               │
        │                             └──────────┘               │
        └──────────────────────────────────────────────────────────┘
```

---

## BC 清單

| BC | Package | 分類 | 一句話職責 |
|---|---|---|---|
| **Catalog** | `com.example.claudecodeclidemo.catalog` | Core Domain | 商品目錄與庫存管理 |
| **Cart** | `com.example.claudecodeclidemo.cart` | Supporting | 購物車品項管理與結帳觸發 |
| **Order** | `com.example.claudecodeclidemo.order` | Core Domain | 訂單生命週期與狀態流轉 |
| **Payment** | `com.example.claudecodeclidemo.payment` | Core Domain | 金流收款與付款狀態管理 |

---

## Catalog BC

### 職責
- 維護 **Product**（商品資訊、定價）和 **Category**（分類樹）
- 維護 **Stock**（庫存量），提供 reserve / release / deduct 三種操作
- 提供商品查詢（列表、單品、關鍵字搜尋）

### Aggregate

#### Product (Aggregate Root)
| 欄位 | 型別 | 說明 |
|---|---|---|
| id | `ProductId` | 全域唯一識別 |
| name | `String` | 商品名稱 |
| sku | `Sku` | 庫存單位編碼 |
| price | `Money` | 定價（不可為負） |
| categoryId | `CategoryId` | 所屬分類 |
| status | `ProductStatus` | ACTIVE / INACTIVE / DISCONTINUED |

#### Stock (Aggregate Root)
| 欄位 | 型別 | 說明 |
|---|---|---|
| productId | `ProductId` | 對應商品（1:1） |
| quantity | `int` | 現有庫存（≥ 0） |
| reserved | `int` | 已保留量（≥ 0，≤ quantity） |

#### Category (Aggregate Root)
| 欄位 | 型別 | 說明 |
|---|---|---|
| id | `CategoryId` | 全域唯一識別 |
| name | `String` | 分類名稱 |
| parentId | `CategoryId?` | 上層分類（根節點為 null） |

### Value Objects
- `Money(amount: BigDecimal, currency: Currency)` — 貨幣金額，不可變
- `Sku(value: String)` — 格式 `[A-Z0-9-]{4,20}`
- `CategoryId(value: UUID)`

### Ports
- **In**: `CreateProductUseCase`、`UpdateStockUseCase`、`QueryProductUseCase`
- **Out**: `ProductRepository`、`StockRepository`、`CategoryRepository`
- **Provided to others**: `CatalogQueryPort`（Cart 用）、`StockReservationPort`（Order 用）

---

## Cart BC

### 職責
- 每位使用者一個 Cart，以 `UserId` 為鍵
- 加入 / 移除 / 調整 CartItem 數量
- 計算小計（向 Catalog 詢價）
- 結帳時呼叫 Order BC 建立訂單，**並清空購物車**

### Aggregate

#### Cart (Aggregate Root)
| 欄位 | 型別 | 說明 |
|---|---|---|
| id | `CartId` | |
| userId | `UserId` | 擁有者 |
| items | `List<CartItem>` | 品項清單（最多 50 項） |
| updatedAt | `Instant` | |

#### CartItem (Entity)
| 欄位 | 型別 | 說明 |
|---|---|---|
| id | `CartItemId` | |
| productId | `ProductId` | |
| quantity | `Quantity` | ≥ 1 |
| unitPrice | `Money` | 詢價快照（下單時再確認） |

### Value Objects
- `Quantity(value: int)` — ≥ 1
- `UserId(value: UUID)`

### Ports
- **In**: `AddItemUseCase`、`UpdateItemUseCase`、`RemoveItemUseCase`、`CheckoutUseCase`
- **Out**: `CartRepository`
- **Consumed from others**: `CatalogQueryPort`（詢價）、`OrderCheckoutPort`（建立訂單）

---

## Order BC

### 職責
- 接收結帳請求，建立 Order Aggregate
- 管理狀態流轉：`CREATED → PAID → FULFILLED` / `CANCELLED`
- 下單時保留庫存；付款成功後扣除；取消或付款失敗則釋放
- 監聽 Payment 事件以驅動狀態轉換

### Aggregate

#### Order (Aggregate Root)
| 欄位 | 型別 | 說明 |
|---|---|---|
| id | `OrderId` | |
| userId | `UserId` | |
| lines | `List<OrderLine>` | 訂單明細（至少 1 筆） |
| status | `OrderStatus` | CREATED / PAID / FULFILLED / CANCELLED |
| totalAmount | `Money` | 各 line 加總 |
| createdAt | `Instant` | |

#### OrderLine (Entity)
| 欄位 | 型別 | 說明 |
|---|---|---|
| id | `OrderLineId` | |
| productId | `ProductId` | |
| productName | `String` | 快照（商品名可改，訂單不受影響） |
| quantity | `Quantity` | |
| unitPrice | `Money` | 下單當下定價快照 |

### Value Objects
- `OrderStatus` — enum + 合法轉換規則
- `OrderId(value: UUID)`
- `Money(amount, currency)`

### Domain Events 發出
- `OrderCreatedEvent`
- `OrderPaidEvent`
- `OrderCancelledEvent`

### Ports
- **In**: `CreateOrderUseCase`（= `OrderCheckoutPort`）、`CancelOrderUseCase`
- **Out**: `OrderRepository`
- **Consumed from others**: `StockReservationPort`（Catalog）、`PaymentPort`（Payment）
- **Listens**: `PaymentSucceededEvent`、`PaymentFailedEvent`

---

## Payment BC

### 職責
- 接收 Order 的付款發起請求，建立 Payment Aggregate
- 對接外部金流（MVP：Fake Gateway）
- 付款結果以 Domain Event 通知 Order

### Aggregate

#### Payment (Aggregate Root)
| 欄位 | 型別 | 說明 |
|---|---|---|
| id | `PaymentId` | |
| orderId | `OrderId` | 對應訂單（1:1） |
| amount | `Money` | 應收金額 |
| method | `PaymentMethod` | CREDIT_CARD / BANK_TRANSFER / MOCK |
| status | `PaymentStatus` | PENDING / SUCCEEDED / FAILED |
| paidAt | `Instant?` | 成功時才填 |

### Value Objects
- `PaymentStatus` — PENDING / SUCCEEDED / FAILED
- `PaymentMethod` — enum

### Domain Events 發出
- `PaymentSucceededEvent`
- `PaymentFailedEvent`

### Ports
- **In**: `InitiatePaymentUseCase`（= `PaymentPort`）、`HandleWebhookUseCase`
- **Out**: `PaymentRepository`、`PaymentGatewayPort`（外部金流）
- **Provided to others**: `PaymentPort`（Order 用）

---

## 整合關係摘要

| 上游 BC | 下游 BC | 模式 | 整合方式 |
|---|---|---|---|
| Catalog | Cart | Open Host Service | `CatalogQueryPort` interface（Cart adapter 實作） |
| Catalog | Order | Customer / Supplier | `StockReservationPort` interface |
| Cart | Order | Customer / Supplier | `OrderCheckoutPort` interface |
| Order ↔ Payment | — | Partnership | `PaymentPort` interface（同步呼叫）+ `PaymentSucceededEvent` / `PaymentFailedEvent`（非同步回呼） |

### 邊界守則
1. 跨 BC **不能直接 import** 對方的 domain 物件
2. 跨 BC 互動**只透過 port interface**，實作在 adapter 層
3. 跨 BC 資料傳遞**用 DTO**，非 domain entity
4. 同步互動：port + adapter（直接呼叫，MVP 不用 message broker）
5. 非同步：Spring `ApplicationEventPublisher`（MVP），v2+ 升 Kafka
