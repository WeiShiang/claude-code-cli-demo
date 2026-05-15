# Cart BC 開發報告

> 生成日期：2026-05-15  
> 開發分支：master  
> BC 分類：Supporting Domain

---

## 實作摘要

### Aggregate

| 元素 | 型別 | 說明 |
|---|---|---|
| `Cart` | Aggregate Root | 每位使用者唯一購物車，上限 50 個 CartItem |
| `CartItem` | Entity | 購物車品項，含 productId、quantity、unitPrice 快照 |

### Value Objects

| VO | 驗證規則 |
|---|---|
| `CartId` | UUID, non-null |
| `CartItemId` | UUID, non-null |
| `UserId` | UUID, non-null |
| `ProductId` | UUID, non-null |
| `OrderId` | UUID, non-null |
| `Quantity` | int ≥ 1 |
| `Money` | amount non-null, currency non-null |

### Use Cases（Port In）

| Interface | Command | 說明 |
|---|---|---|
| `AddItemUseCase` | `AddItemCommand` | 加入品項；購物車不存在則自動建立；同商品累加數量 |
| `UpdateItemUseCase` | `UpdateItemCommand` | 修改品項數量 |
| `RemoveItemUseCase` | `RemoveItemCommand` | 移除品項 |
| `CheckoutUseCase` | `CheckoutCommand` | 驗證商品可用性 → 建立訂單 → 清空購物車 → 發布事件 |

### Domain Events

| Event | 觸發時機 | 欄位 |
|---|---|---|
| `CartCheckedOutEvent` | `CheckoutUseCase` 成功建立訂單並清空購物車 | `cartId`, `userId`, `orderId`, `items: List<CartItemSnapshot>`, `occurredAt` |

### REST API（Adapter In）

| Method | Path | 說明 | 回應 |
|---|---|---|---|
| POST | `/api/carts/{userId}/items` | 加入品項 | 204 No Content |
| PUT | `/api/carts/{userId}/items/{productId}` | 更新數量 | 204 No Content |
| DELETE | `/api/carts/{userId}/items/{productId}` | 移除品項 | 204 No Content |
| POST | `/api/carts/{userId}/checkout` | 結帳 | 200 `{"orderId":"..."}` |

---

## TDD 循環記錄

| Phase | 說明 | Commit |
|---|---|---|
| 🔴 RED | 新增 19 個測試，38 個編譯錯誤（實作不存在） | `2055819` |
| 🟢 GREEN | 實作 Domain / Application / Adapter stub | `377a9ad` |
| 🔵 REFACTOR | `Cart.addItem()` 改為 Early Return 模式 | `7d3dbcf` |
| 🔧 ADAPTER | 實作 Web Controller + JPA Persistence Adapter | `5249163` |
| 📊 COVERAGE | 補充 VO 覆蓋率測試，達到 domain 97.2% | `9978245` |

---

## 覆蓋率數字

> 測試執行日期：2026-05-15

| 層級 | 覆蓋行 / 總行 | Line Coverage |
|---|---|---|
| **Domain** | 137 / 141 | **97.2%** |
| **Application** | 34 / 42 | **81.0%** |
| **Adapter** | 70 / 73 | **95.9%** |
| **整體（含 Catalog）** | 537 / 563 | **95.4%** |

---

## DDD Rules 合規結果

| 規則 | 結果 | 說明 |
|---|---|---|
| Domain 純度 | ✅ PASS | `domain/` 無 Spring / JPA import |
| 命名合規 | ✅ PASS | 全部符合 `ubiquitous-language.md` 詞彙表 |
| 無 Utils/Helper/Manager | ✅ PASS | 無此類命名 |
| 方法 ≤ 20 行 | ✅ PASS | 最長方法 12 行（`CartService.checkout`） |
| Class ≤ 200 行 | ✅ PASS | 最大 91 行（`Cart.java`） |
| Early Return Pattern | ✅ PASS | `Cart.addItem()` 使用 Guard Clause |
| CQS | ✅ PASS | Command 方法無回傳值（`CheckoutUseCase.checkout()` 回傳 `OrderId` 為設計例外） |
| 跨 BC 隔離 | ✅ PASS | Cart BC 定義自己的 `ProductId`、`Money`；透過 Port interface 與 Catalog/Order 隔離 |

---

## Invariant 測試覆蓋

| Invariant | 規則 | 負向測試 |
|---|---|---|
| C-1 | Cart 最多 50 個 CartItem | `addItem_fiftyFirstDistinctProduct_throwsCartItemLimitExceededException_C1` |
| C-2 | 同 productId 累加數量 | `addItem_sameProductTwice_accumulatesQuantity_C2` |
| C-3 | CartItem 數量 ≥ 1 | `create_withValueLessThanOne_throwsInvalidQuantityException` |
| C-4 | 空 Cart 不可 checkout | `checkout_emptyCart_throwsEmptyCartException_C4`（domain + service 兩層） |
| C-5 | checkout 前確認商品為 ACTIVE | `checkout_productNotActive_throwsProductNotAvailableException_C5` |

---

## 已知限制與後續待辦

### Stub Adapters（待 Phase 6+ 實作）

| Adapter | 現況 | 後續 |
|---|---|---|
| `CatalogQueryAdapter` | 拋出 `UnsupportedOperationException` | 整合 Catalog BC 的 `ProductService` 或 OpenHost API |
| `OrderCheckoutAdapter` | 拋出 `UnsupportedOperationException` | 整合 Order BC 的 `CreateOrderUseCase` |

### 功能待辦

- [ ] `GET /api/carts/{userId}` — 查詢購物車狀態 API
- [ ] Cart checkout 冪等性保護（X-4 跨 BC 不變量）
- [ ] 結帳後重試不重複建立訂單
- [ ] `ProductPriceChangedEvent` 訂閱 — 更新購物車品項單價快照（v2）
- [ ] 購物車過期清除機制（TTL）

### 測試待補

- [ ] `CartService.updateItem()` / `removeItem()` Service 層測試
- [ ] `@WebMvcTest` 補充 400 / 422 邊界測試
- [ ] Application Layer 覆蓋率從 81% 提升至 90%+
