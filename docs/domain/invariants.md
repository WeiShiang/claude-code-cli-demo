# Domain Invariants — Shopping Mall

> **不變量（Invariant）** 是 Aggregate 在任何時刻都必須成立的業務規則。  
> 這些規則**由 Aggregate Root 強制執行**，不依賴呼叫方遵守。  
> 違反不變量時應拋出具名的 `DomainException`，而非通用的 `IllegalArgumentException`。

---

## Catalog BC

### Product Invariants

| # | 規則 | 違反時拋出 | 執行層 |
|---|---|---|---|
| P-1 | 商品名稱不可為空白或空字串 | `InvalidProductNameException` | Aggregate Root |
| P-2 | 定價 (`price`) 必須 ≥ 0（可以免費，不可為負） | `InvalidPriceException` | Aggregate Root |
| P-3 | SKU 格式必須符合 `[A-Z0-9-]{4,20}` | `InvalidSkuException` | Aggregate Root（VO 驗證） |
| P-4 | SKU 在系統內必須唯一 | `DuplicateSkuException` | **Application Service**（需 Repository 查詢，Aggregate Root 無法獨立執行） |
| P-5 | `DISCONTINUED` 的商品不可被重新上架（`ACTIVE`） | `InvalidProductStateTransitionException` | Aggregate Root |
| P-6 | `CategoryId` 必須指向一個已存在的分類 | `CategoryNotFoundException` | **Application Service**（需 Repository 查詢，Aggregate Root 無法獨立執行） |

### Stock Invariants

| # | 規則 | 違反時拋出 |
|---|---|---|
| S-1 | `quantity`（現有庫存）必須 ≥ 0 | `InvalidStockQuantityException` |
| S-2 | `reserved`（保留量）必須 ≥ 0 且 ≤ `quantity` | `InvalidStockReservationException` |
| S-3 | 可用庫存 (`quantity - reserved`) 必須 ≥ 0；reserve 操作前須確認 | `InsufficientStockException` |
| S-4 | deduct 的數量不可超過 `reserved` | `InvalidDeductAmountException` |
| S-5 | release 的數量不可超過 `reserved` | `InvalidReleaseAmountException` |

---

## Cart BC

### Cart Invariants

| # | 規則 | 違反時拋出 |
|---|---|---|
| C-1 | 每個 Cart 最多 50 個不同 CartItem | `CartItemLimitExceededException` |
| C-2 | 同一個 `productId` 在同一個 Cart 只能有一個 CartItem（重複加入則累加） | *(由 addItem 邏輯保證，非例外)* |
| C-3 | CartItem 數量必須 ≥ 1；調整為 0 等同 removeItem | `InvalidQuantityException` |
| C-4 | 空的 Cart（0 個 CartItem）不可執行 checkout | `EmptyCartException` |
| C-5 | checkout 前需確認所有 CartItem 的商品仍為 `ACTIVE` | `ProductNotAvailableException` |

---

## Order BC

### Order Invariants

| # | 規則 | 違反時拋出 |
|---|---|---|
| O-1 | Order 至少需要 1 個 OrderLine | `EmptyOrderException` |
| O-2 | `totalAmount` 必須等於所有 `OrderLine.unitPrice × quantity` 之加總 | *(由 constructor 計算保證)* |
| O-3 | `totalAmount` 必須 > 0 | `InvalidOrderAmountException` |
| O-4 | 狀態轉換必須符合合法路徑（見狀態機） | `InvalidOrderStateTransitionException` |
| O-5 | 只有 `CREATED` 狀態的訂單可以被取消（使用者主動） | `OrderCancellationNotAllowedException` |
| O-6 | OrderLine 的 `unitPrice` 與 `productName` 在建立後不可被修改（快照） | *(由 immutable fields 保證)* |
| O-7 | 同一筆訂單不可重複付款（`PAID` 狀態收到 `PaymentSucceededEvent` 應忽略） | `OrderAlreadyPaidException` |

### OrderLine Invariants

| # | 規則 | 違反時拋出 |
|---|---|---|
| OL-1 | 數量必須 ≥ 1 | `InvalidQuantityException` |
| OL-2 | 單價必須 ≥ 0 | `InvalidPriceException` |
| OL-3 | 商品名快照不可為空 | `InvalidProductNameException` |

---

## Payment BC

### Payment Invariants

| # | 規則 | 違反時拋出 |
|---|---|---|
| PM-1 | 付款金額必須 > 0 | `InvalidPaymentAmountException` |
| PM-2 | 每筆 `orderId` 只能有一個 `Payment`（不可重複建立） | `DuplicatePaymentException` |
| PM-3 | 狀態只能從 `PENDING` 轉至 `SUCCEEDED` 或 `FAILED` | `InvalidPaymentStateTransitionException` |
| PM-4 | `SUCCEEDED` 或 `FAILED` 的 Payment 不可再次更新狀態（終態） | `InvalidPaymentStateTransitionException` |
| PM-5 | `paidAt` 只有在狀態為 `SUCCEEDED` 時才有值 | *(由 succeed() method 保證)* |

---

## 跨 BC 不變量

| # | 規則 | 執行層 |
|---|---|---|
| X-1 | 同一個 `productId` 在系統中任意時刻，`Stock.reserved` ≤ `Stock.quantity` | Catalog: `StockReservationPort` |
| X-2 | 每筆訂單的 `totalAmount` 必須等於對應 Payment 的 `amount` | Order 建立 Payment 時傳入 |
| X-3 | `OrderLine.unitPrice` 快照一旦建立不可變（即使 Catalog 調整定價） | Order: immutable field |
| X-4 | Cart checkout 後 Cart 必須被清空（idempotent：重試不會重複建立訂單） | Cart: `CheckoutUseCase` 實作 |

---

## 執行原則

1. **Aggregate Root 是唯一守門員** — 外部只能呼叫 Aggregate Root 的方法，不可直接操作內部 Entity 或 VO。
2. **Fail Fast** — 不變量違反時立即拋出具名例外，不做靜默回退。
3. **不變量在測試中必須有對應的 unit test** — 每條規則至少一個 Red 測試案例（見 `invariant-tests.md`）。
4. **跨 BC 不變量的執行責任歸屬明確** — 由上游 Port 或 Use Case 在整合邊界強制執行。
