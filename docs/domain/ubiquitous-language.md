# Ubiquitous Language — Shopping Mall

> 統一語言是團隊與程式碼共用的詞彙表。  
> **術語必須一致出現在**：需求文件、API 命名、程式碼識別符（class/method/field）、資料庫欄位、日誌與事件名稱。

---

## Catalog BC

| 術語 | 英文 | 定義 |
|---|---|---|
| **商品** | Product | 可銷售的單一品項，具有名稱、價格、SKU 與所屬分類 |
| **庫存單位** | SKU (Stock Keeping Unit) | 商品的唯一庫存編碼，格式 `[A-Z0-9-]{4,20}` |
| **分類** | Category | 商品的樹狀分群（例：電子產品 > 手機） |
| **分類 ID** | CategoryId | 分類的全域唯一識別值（UUID） |
| **庫存** | Stock | 特定商品的可用數量，與 Product 1:1 對應 |
| **保留** | Reserve | 下單時預先鎖定庫存，防止超賣；尚未真正扣除 |
| **釋放** | Release | 訂單取消或付款失敗時，歸還被保留的庫存 |
| **扣除** | Deduct | 付款成功後，將保留量轉為永久扣除 |
| **定價** | Price | 商品的銷售金額（Money 型別），不可為負數 |
| **金額** | Money | 由數值（BigDecimal）與幣別（Currency）組成的值物件 |
| **商品狀態** | ProductStatus | ACTIVE（上架）/ INACTIVE（下架）/ DISCONTINUED（停售） |

---

## Cart BC

| 術語 | 英文 | 定義 |
|---|---|---|
| **購物車** | Cart | 使用者暫存欲購商品的容器，每人唯一 |
| **購物車品項** | CartItem | 購物車中的單一商品記錄，含商品 ID、數量、單價快照 |
| **購物車品項 ID** | CartItemId | CartItem 的唯一識別值（UUID） |
| **購物車品項快照** | CartItemSnapshot | 結帳當下的 CartItem 不可變記錄，用於 `CartCheckedOutEvent`；含 `productId`、`quantity`、`unitPrice` |
| **數量** | Quantity | 品項的購買數量，最小值為 1（**語意收斂**：不允許 0，若需移除請呼叫 `removeItem`） |
| **單價快照** | Unit Price Snapshot | 加入購物車時的定價快照；結帳前會重新向 Catalog 確認 |
| **小計** | Subtotal | 購物車內所有品項的金額加總 |
| **加入品項** | Add Item | 將商品加入購物車，若同商品已存在則累加數量 |
| **調整數量** | Update Quantity | 修改特定品項的數量（設為 0 等同移除） |
| **移除品項** | Remove Item | 從購物車刪除特定品項 |
| **結帳** | Checkout | 提交購物車內容以建立訂單，完成後清空購物車 |

---

## Order BC

| 術語 | 英文 | 定義 |
|---|---|---|
| **訂單** | Order | 使用者結帳後建立的購買契約，含訂單明細與狀態 |
| **訂單明細** | OrderLine | 訂單中每一個商品的購買記錄（含定價快照，下單後不隨商品定價變動） |
| **訂單明細 ID** | OrderLineId | OrderLine 的唯一識別值（UUID） |
| **訂單明細快照** | OrderLineSnapshot | 建立訂單當下的 OrderLine 不可變記錄，用於 `OrderCreatedEvent`、`OrderPaidEvent`；含 `productId`、`productName`、`quantity`、`unitPrice` |
| **取消原因** | CancellationReason | 訂單取消的原因分類：`USER_REQUEST`（使用者主動）/ `PAYMENT_FAILED`（付款失敗）/ `SYSTEM`（系統處理） |
| **訂單狀態** | OrderStatus | 訂單的生命週期狀態（見下方狀態機） |
| **建立** | CREATED | 訂單剛建立，庫存已保留，等待付款 |
| **已付款** | PAID | 付款成功，庫存正式扣除 |
| **已完成** | FULFILLED | 商品已出貨/完成，訂單結案 |
| **已取消** | CANCELLED | 訂單取消，保留庫存已釋放 |
| **定價快照** | Price Snapshot | 下單當下記錄的商品單價，後續商品定價變更不影響既有訂單 |
| **名稱快照** | Name Snapshot | 下單當下記錄的商品名稱，後續商品改名不影響既有訂單 |
| **訂單金額** | Total Amount | 所有 OrderLine 金額之加總 |

### 訂單狀態機

```
CREATED ──(付款成功)──► PAID ──(出貨完成)──► FULFILLED
   │                    │
   │(付款失敗/手動取消)  │(訂單取消)
   ▼                    ▼
CANCELLED           CANCELLED
```

**合法轉換**：
- `CREATED → PAID`：收到 `PaymentSucceededEvent`
- `CREATED → CANCELLED`：付款失敗（`PaymentFailedEvent`）或使用者主動取消
- `PAID → FULFILLED`：出貨完成（v2 Fulfillment BC 觸發）
- `PAID → CANCELLED`：特殊退款流程（v2）
- 其餘轉換皆為非法，需拋出 `InvalidOrderStateTransitionException`

---

## Payment BC

| 術語 | 英文 | 定義 |
|---|---|---|
| **付款** | Payment | 針對一筆訂單發起的收款流程，含付款方式與狀態 |
| **付款狀態** | PaymentStatus | PENDING / SUCCEEDED / FAILED |
| **待處理** | PENDING | 已向金流商提交，等待回應 |
| **成功** | SUCCEEDED | 金流商確認收款成功 |
| **失敗** | FAILED | 金流商拒絕或逾時 |
| **付款方式** | PaymentMethod | CREDIT_CARD / BANK_TRANSFER / MOCK（測試用） |
| **金流商** | Payment Gateway | 外部付款服務（MVP 使用 FakeGateway adapter） |
| **Webhook** | Webhook | 金流商主動回呼系統，告知付款結果 |
| **模擬閘道** | Fake Gateway | MVP 階段使用的假金流商，固定回傳成功或失敗 |

---

## 跨 BC 通用術語

| 術語 | 英文 | 定義 |
|---|---|---|
| **使用者** | User | 已登入的系統使用者，以 `UserId`（UUID）識別 |
| **領域事件** | Domain Event | BC 內部狀態變更後對外廣播的事件（過去式命名，例：`OrderCreated`） |
| **Port** | Port | BC 與外界互動的抽象介面（Hexagonal 術語） |
| **Adapter** | Adapter | Port 的具體實作，位於 `adapter/in` 或 `adapter/out` |
| **Aggregate Root** | Aggregate Root | 負責保護 Aggregate 不變量的入口實體 |
| **值物件** | Value Object | 無唯一識別、以值相等的不可變物件 |
| **不變量** | Invariant | Aggregate 任何時刻必須成立的業務規則 |

---

## 禁用詞 / 歧義詞對照

| 禁用 / 歧義 | 應使用 | 說明 |
|---|---|---|
| `item` (單獨使用) | `CartItem` 或 `OrderLine` | 依語境明確化 |
| `price` (商品價格) | `Money` / `unitPrice` | 強調型別，而非原始數字 |
| `status` (無 BC 前綴) | `OrderStatus`、`PaymentStatus` | 需有明確的 BC context |
| `cancel` (動詞，無主詞) | `cancelOrder` / `refundPayment` | 依 BC 使用正確的動詞 |
| `user_id` (snake_case) | `userId` (camelCase in code) | 程式碼統一 camelCase |
