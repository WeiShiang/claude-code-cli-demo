# Domain Events(領域事件)

> 三個 Bounded Context 發布的領域事件目錄。
> 事件命名**已經發生的事實** — 過去式、用發布者的語言、不可變。
> 事件屬於每個 BC 的公開契約:改 payload 或語意都算 breaking change。

---

## 命名與規範

### 命名規則
- `<BoundedContext>.<AggregateName><PastTenseVerb>` — 如 `Cart.CheckedOut`、`Order.Placed`。
- 一律過去式。沒有 `WillBe`、沒有命令式(`PlaceOrder` 是命令,不是事件)。
- 單數。`Cart.ItemAdded`,不是 `Cart.ItemsAdded`。

### Envelope(每個事件都帶這層)

```json
{
  "eventId":       "uuid",          // 每個事件唯一,用於去重
  "eventType":     "Cart.CheckedOut",
  "eventVersion":  "1",             // schema 版本,有破壞性變更時要升版
  "occurredAt":    "ISO-8601 UTC",
  "producedBy":    "cart-service",
  "aggregateId":   "uuid",          // 發出此事件的 aggregate root id
  "aggregateType": "Cart",
  "correlationId": "uuid",          // 跨 BC 把同一連串事件串起來
  "causationId":   "uuid",          // 導致此事件的上一個事件或命令
  "payload":       { /* 各事件不同,見下 */ }
}
```

### 版本演進規則
- **相容性改動**(新增 optional 欄位):`eventVersion` 不變,但要在文件補上。
- **破壞性改動**(改名、移除、改型別、語意改變):發布新版本 `v2`;`v1` 持續發布到訂閱方遷移完,再下線。
- 同一版本內,既有欄位的意義絕對不能變。

### 冪等性契約
- 訂閱方**必須**假設事件是 **at-least-once**(至少送一次)送達。
- 訂閱方**必須**用 `eventId` 去重。Order 對 `Cart.CheckedOut` 的 dedup 是核心 invariant(ORD-INV-006)。
- 事件與 aggregate 寫入要在同一個 DB 交易中寫到 outbox — 不會發生「狀態改了但事件沒發」或反過來。

---

## Catalog Context — 發布的事件

### `Catalog.ProductPublished` (v1)
某個商品從 `Draft` 轉為 `Published`,現在對客人可見、可賣。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `productId` | UUID | Catalog 的商品識別 |
| `sku` | string | 對客人可見的穩定 SKU |
| `name` | string | |
| `listPrice` | Money(`{amount, currency}`) | |
| `categoryIds` | UUID[] | |
| `occurredAt` | ISO-8601 | 商品發布時間（envelope 通用欄位，語意同 `publishedAt`） |

**訂閱方**:搜尋索引器;(未來)Cart Context 用於 product directory 預載。

### `Catalog.ProductUnpublished` (v1)
商品下架。**Cart 中含此商品的項目需 revalidate**;Order 不受影響(它持有快照)。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `productId` | UUID | |
| `sku` | string | |
| `reason` | enum | `Discontinued` \| `Withdrawn` \| `Replaced` |
| `occurredAt` | ISO-8601 | 商品下架時間（envelope 通用欄位，語意同 `unpublishedAt`） |

**訂閱方**:搜尋索引器(從索引移除);Cart Context(對含此商品的 active cart 設 `NeedsRevalidation`)。

### `Catalog.PriceChanged` (v1)
標價變動。**Cart 中含此商品的項目需更新 currentPrice 並設標記**;Order 不受影響。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `productId` | UUID | |
| `sku` | string | |
| `previousPrice` | Money | |
| `newPrice` | Money | |
| `effectiveAt` | ISO-8601 | 新價生效時間 |

**訂閱方**:搜尋索引器;**Cart Context**(關鍵訂閱者,觸發 CART-INV-008)。

### `Catalog.ProductArchived` (v1)
商品轉為 `Archived` 終局狀態,完全不可變。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `productId` | UUID | |
| `occurredAt` | ISO-8601 | 商品封存時間（envelope 通用欄位，語意同 `archivedAt`） |

**訂閱方**:搜尋索引器;Cart Context(視同 `Unpublished` 處理 — 設標記)。

---

## Cart Context — 發布的事件

### `Cart.Created` (v1)
為客人(或匿名訪客)建立了一張新的 Active Cart。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `cartId` | UUID | |
| `customerId` | UUID? | 會員 id;訪客時為 null |
| `sessionId` | string? | 匿名訪客的 session id;會員時為 null |
| `createdAt` | ISO-8601 | |

**訂閱方**:Analytics。

### `Cart.ItemAdded` (v1)
一個商品被加入 cart。重複加入既有商品時也發此事件(payload 描述新的 quantity)。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `cartId` | UUID | |
| `productId` | UUID | |
| `sku` | string | |
| `addedQuantity` | int | 此次新增的數量 |
| `newQuantity` | int | 累加後 CartItem 的最終 quantity |
| `addedAt` | ISO-8601 | |

**訂閱方**:Analytics(理解客人購物行為)。

### `Cart.ItemRemoved` (v1)
某個 CartItem 被完全移除(或 `UpdateQuantity(0)` 觸發)。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `cartId` | UUID | |
| `productId` | UUID | |
| `removedAt` | ISO-8601 | |

**訂閱方**:Analytics。

### `Cart.QuantityUpdated` (v1)
CartItem 的數量被改動(非 0)。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `cartId` | UUID | |
| `productId` | UUID | |
| `previousQuantity` | int | |
| `newQuantity` | int | |
| `updatedAt` | ISO-8601 | |

**訂閱方**:Analytics。

### `Cart.MarkedForRevalidation` (v1)
Cart 因為訂閱到 `Catalog.PriceChanged` 或 `Catalog.ProductUnpublished` 而被標記。客人下次開啟時 UI 應顯示警示。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `cartId` | UUID | |
| `affectedProductIds` | UUID[] | 受影響的商品 |
| `reason` | enum | `PriceChanged` \| `ProductUnpublished` \| `ProductArchived` |
| `markedAt` | ISO-8601 | |

**訂閱方**:通知服務(可選擇發 push / email 提醒客人);Analytics。

### `Cart.CheckedOut` (v1) ⭐
客人完成 checkout。Cart 自身已轉為 `CheckedOut` 終局狀態。**這是 Order Context 建立 Order 的唯一觸發點**。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `cartId` | UUID | |
| `customerId` | UUID? | 訪客 cart 在 checkout 時必須先轉會員,此處不應為 null |
| `lines` | CartLineSnapshot[] | 每筆:`{productId, sku, name, quantityAtCheckout, priceAtCheckout}` |
| `checkedOutAt` | ISO-8601 | |

**訂閱方**:**Order Context**(關鍵訂閱者,執行 PlaceOrder);Analytics。

> ⚠️ payload 中的 `priceAtCheckout` 只是 Cart 當下看到的 currentPrice,**不是**最終訂單金額。Order 在收到事件後會重新讀 Catalog 取完整快照、再計算。這個欄位主要給 Analytics 用、以及對帳時比對。

### `Cart.Abandoned` (v1)
客人明確放棄 cart,或長期無互動由系統判定放棄。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `cartId` | UUID | |
| `customerId` | UUID? | |
| `abandonedBy` | enum | `Customer` \| `System` |
| `lastActivityAt` | ISO-8601 | |
| `abandonedAt` | ISO-8601 | |

**訂閱方**:行銷服務(可發 reminder email);Analytics。

### `Cart.Expired` (v1)
Cart TTL 到期(預設 90 天無互動)。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `cartId` | UUID | |
| `customerId` | UUID? | |
| `expiredAt` | ISO-8601 | |

**訂閱方**:Analytics。

---

## Order Context — 發布的事件

### `Order.Placed` (v1) ⭐
一張 Order 在 Order Context 內成立。觸發點是 Order Context 訂閱到 `Cart.CheckedOut` 後內部執行 `PlaceOrder`。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `orderId` | UUID | |
| `customerId` | UUID | |
| `cartCheckedOutEventId` | UUID | 來源 cart 的 checkout 事件 id(用於追溯與冪等) |
| `lines` | OrderLineSnapshot[] | 每筆:`{productId, sku, name, unitPrice, quantity, lineTotal}` |
| `subtotal` | Money | |
| `total` | Money | v1 = subtotal,凍結 |
| `currency` | string | ISO 4217 |
| `placedAt` | ISO-8601 | |

**訂閱方**:通知服務(寄訂單成立信);Analytics;(未來)Payment Context、Inventory Context。

### `Order.MarkedAsPaid` (v1)
Order 被確認已收款,轉為 `Paid`。v1 由人工 / 對帳觸發,未來由 Payment Context 訂閱事件後觸發。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `orderId` | UUID | |
| `paidAt` | ISO-8601 | |

**訂閱方**:通知服務;Analytics;(未來)Inventory(commit reservation)。

### `Order.Shipped` (v1)
Order 出貨。v1 不串物流商,純資訊性事件。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `orderId` | UUID | |
| `shippedAt` | ISO-8601 | |
| `trackingReference` | string? | 可選 |

**訂閱方**:通知服務。

### `Order.Completed` (v1)
Order 完成(送達或等同狀態)。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `orderId` | UUID | |
| `completedAt` | ISO-8601 | |

**訂閱方**:Analytics。

### `Order.Cancelled` (v1)
Order 被取消。

| 欄位 | 型別 | 說明 |
|---|---|---|
| `orderId` | UUID | |
| `cancelledBy` | enum | `Customer` \| `Merchant` \| `System` |
| `reason` | string | 自由文字,記下取消原因 |
| `previousStatus` | enum | 取消前的狀態,用於下游補償判斷 |
| `cancelledAt` | ISO-8601 | |

**訂閱方**:通知服務;Analytics;(未來)Payment Context(若已收款則退款)、Inventory(若已預留則釋放)。

---

## 跨 BC 事件流(完整)

### 流程一:從加入購物車到訂單成立(Happy Path)

```
[Catalog Context]
  Catalog.ProductPublished ──┐
                             │
                             ▼
[Cart Context] (訂閱,用於 product directory)
  客人 ─AddToCart──► Cart
                       │
                       ├─emit─► Cart.ItemAdded
                       │
                       │ (重複多次操作)
                       │
  客人 ─Checkout──► Cart
                       │
                       ├─驗證(非空、無 NeedsRevalidation 標記)
                       ├─狀態轉移 Active → CheckedOut
                       └─emit─► Cart.CheckedOut ⭐
                                      │
                                      ▼
                              [Order Context]
                                      │
                                      ├─用 eventId 去重(冪等)
                                      ├─讀 Catalog,組 OrderLineSnapshot
                                      ├─計算 subtotal / total
                                      ├─建立 Order(status = PendingPayment)
                                      └─emit─► Order.Placed
                                                    │
                                                    ▼
                                            (下游訂閱者:通知、Analytics 等)
```

### 流程二:Catalog 變動觸發 Cart revalidation

```
[Catalog Context]
  商家 ─UpdatePrice──► Product
                          │
                          └─emit─► Catalog.PriceChanged
                                          │
                                          ▼
                                  [Cart Context] (訂閱)
                                          │
                                          ├─找出所有含此 productId 的 active cart
                                          ├─更新各 cart 的 ProductRef.currentPrice
                                          ├─設 NeedsRevalidation = true
                                          └─emit─► Cart.MarkedForRevalidation
                                                          │
                                                          ▼
                                                  (通知服務可發提醒)

之後:
  客人開啟 cart ──► UI 顯示「商品價格已變動,請確認」
  客人按 Checkout ──► 因 NeedsRevalidation 標記被拒(CART-INV-006)
  客人確認新價 ──► 標記清除 ──► 重新 Checkout ──► Cart.CheckedOut ──► (流程一接上)
```

### 流程三:Order 取消

```
[Order Context]
  客服或客人 ─CancelOrder──► Order
                                 │
                                 ├─驗狀態(必須在 PendingPayment / Paid / Shipped 之一)
                                 ├─狀態轉移 → Cancelled
                                 └─emit─► Order.Cancelled
                                                 │
                                                 ▼
                                         (下游訂閱者處理通知)
                                         (未來 Payment 訂閱後做退款)

注意:Cart Context 不需要訂閱 Order.Cancelled。Cart 在 checkout 後就進入終局狀態,
與之後 Order 的狀態無關。若客人想再買,要建一個新的 Cart。
```

---

## 反模式(以下都不要做)

- ❌ **把事件命名成命令**:`PlaceOrder`(命令) vs `Order.Placed`(事件)。
- ❌ **在事件裡放決策**:事件只報告事實,不下指令。Cart 不該發 `OrderShouldBeCreated`,只發 `Cart.CheckedOut`,讓 Order 自己決定怎麼做。
- ❌ **Order 直接讀 Cart 的資料庫**:Order 只訂閱 `Cart.CheckedOut` 事件,payload 帶足夠資訊就是契約。需要更多資訊就重讀 Catalog。
- ❌ **Cart 直接呼叫 Order 的 API 建單**:破壞 publish-subscribe 解耦。事件機制讓 Cart 不需要知道誰在訂閱、訂閱了會做什麼。
- ❌ **同一事件版本內新增必填欄位**:破壞性改動,要升版號。
- ❌ **payload 過度精簡**:`Cart.CheckedOut` 必須帶 cartId、customerId、lines 完整內容。光帶 cartId 讓訂閱方再去查,等於把 Cart DB 變成 Order 的依賴,破壞了事件作為契約的價值。
- ❌ **payload 過度膨脹**:不要把 Catalog 的完整商品資訊都塞進 `Cart.CheckedOut`。事件 payload 是「契約所需的最小集合」,Order 需要的 Catalog 細節是 Order 自己去讀 Catalog 補的。
