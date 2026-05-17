# Bounded Contexts(限界上下文)

> 購物商城系統的戰略 DDD 設計文件,涵蓋三個 Bounded Context:**Catalog**、**Cart**、**Order**。
> 每個 BC 定義一個**語意邊界**,在邊界內模型、語言與規則內部一致。
> 跨出邊界後,同一個詞可能代表完全不同的東西 — 這是刻意的。

---

## Context Map(總覽)

```
                       ┌───────────────────────────┐
                       │     Catalog Context       │
                       │   (支援子域 / 上游)        │
                       └──────┬────────────┬───────┘
                              │            │
                snapshot (U/D)│            │snapshot (U/D)
                              ▼            ▼
                       ┌───────────┐  ┌──────────────────┐
                       │    Cart   │  │      Order       │
                       │  Context  │  │      Context     │
                       │(支援/暫存) │  │  (核心域 / Core)  │
                       └─────┬─────┘  └────────┬─────────┘
                             │                 │
                             │  CartCheckedOut │
                             └────────────────►│
                                  (Customer/Supplier)
```

| BC | 戰略定位 | 核心職責 | 一致性 |
|---|---|---|---|
| Catalog | 支援子域 (Supporting) | 商品的「定義」 — 是什麼、長什麼樣、賣多少 | 強一致,讀多寫少 |
| Cart | 支援子域 (Supporting) | 客人「正在考慮」買什麼 — 暫存、可棄置 | 弱一致可接受,高頻 |
| **Order** | **核心域 (Core)** | 已成立的商業承諾 — 不可悄悄丟棄 | 強一致,需稽核 |

### 為什麼 Cart 獨立成 BC

很多系統把 Cart 跟 Order 放在一起。我們刻意分開,因為它們在四個面向上根本不是同類東西:

| 面向 | Cart | Order |
|---|---|---|
| 一致性需求 | 最終一致 OK,偶爾丟失可接受 | 強一致,任何丟失都是事故 |
| 生命週期 | 短暫(分鐘到天),可被使用者或系統棄置 | 長期(訂單存在數年,法規要求保存) |
| 負載特性 | 高頻讀寫,每次瀏覽都會碰 | 寫入頻率低、查詢需精準 |
| 商業承諾 | 零 — 改 cart 不會欠任何人東西 | 高 — 改 order 牽涉錢、貨、稽核 |
| 適合的儲存 | Redis / KV store | RDBMS,有完整 transaction |

混在一起會讓我們同時用「商業承諾」的嚴謹度去處理瀏覽流量,或反過來用「暫存可棄置」的輕鬆度去處理真實訂單。兩種錯誤都很糟。

---

## BC-1:Catalog Context(商品目錄)

### 用途
管理**可以賣什麼**的「定義與展示」。讀多寫少,針對瀏覽與搜尋優化。是商品 metadata 的真實來源。

### 戰略定位
- **類型**:支援子域 (Supporting Subdomain)
- **變動性**:中等 — 商品文案、圖片、價格策略常變;結構面變化緩慢。
- **投資原則**:接近現成方案。用通用搜尋基礎設施(Elasticsearch / Algolia),自有邏輯只限定價規則與目錄組織。

### 邊界

**邊界內**
- Product(SKU、名稱、描述、媒體、屬性)
- Category 與分類體系
- ListPrice(目錄層級的標價)
- 商品生命週期:`Draft → Published → Archived`
- 可搜尋性、可發現性

**明確排除**
- 庫存數量 → 不在本次三個 BC 的範圍內(未來會獨立 Inventory BC)
- 訂單行的價格 → 屬於 Order Context(下單當下取快照)
- 購物車裡商品的暫存資訊 → 屬於 Cart Context(它自己也取快照)
- 評價、促銷活動 → v1 不做

### Ubiquitous Language(節錄)
完整詞彙表見 `ubiquitous-language.md`。Catalog 限定詞:**Product**、**SKU**、**ListPrice**、**Category**、**Attribute**。

### 整合方式

| 方向 | 對象 | 模式 | 說明 |
|---|---|---|---|
| → Cart | Cart Context | Customer / Supplier(上游) | Cart 加入商品時讀 Catalog 一次,取**輕量快照**(顯示用),期間商品改價時 Cart 會 revalidate。 |
| → Order | Order Context | Customer / Supplier(上游) | 結帳當下從 Cart 的內容讀 Catalog **一次**,取**完整快照**並凍結。Order 之後不再讀 Catalog。 |
| → 搜尋索引 | (內部) | Published Language | 發布 `Catalog.ProductPublished`、`Catalog.PriceChanged` 等事件。 |

### 關鍵不變量
詳見 `invariants.md`(CAT-INV-*)。

---

## BC-2:Cart Context(購物車)

### 用途
管理客人**「正在考慮買什麼」**的暫存狀態。它是個瀏覽工件,不是商業承諾。可被棄置、可過期、可被多裝置同步。

### 戰略定位
- **類型**:支援子域 (Supporting Subdomain)
- **變動性**:低(模型本身穩定),但**運行特性高**(每次點擊都碰)。
- **投資原則**:模型做小、做對。把工程精力放在高可用、低延遲、跨裝置同步、過期清理。

### 邊界

**邊界內**
- Cart Aggregate(每個客人的當前購物車)
- CartItem(車內一行)
- ProductRef(輕量快照:productId、sku、name、currentPrice、thumbnailUrl)
- Cart 生命週期:`Active → CheckedOut | Abandoned | Expired`
- 重新驗證(revalidate):當 Catalog 改價或下架時,Cart 標記受影響項目並通知 UI
- 跨裝置 / 訪客→會員的合併

**明確排除**
- 真正下單 → 屬於 Order Context(Cart 只發 `CartCheckedOut` 事件,不直接建 Order)
- 庫存可用性的權威判斷 → Cart 顯示「可能有」(由 Catalog 提示),Order 才做最終預留
- 折扣 / 促銷計算 → v1 不做(只顯示 Catalog 的 ListPrice)
- 任何金錢承諾 — Cart 裡的金額是「估算」,實際付款金額由 Order 凍結

### Ubiquitous Language(節錄)
**Cart**、**CartItem**、**ProductRef**、**AddToCart**、**RemoveFromCart**、**UpdateQuantity**、**Checkout**、**Abandon**。

> ⚠️ Cart 的「Product」是 **ProductRef** — 一個輕量、會 revalidate 的快照,**不等於** Catalog 的 `Product`,也**不等於** Order 的 `OrderLineSnapshot`。三者是不同型別。

### 整合方式

| 方向 | 對象 | 模式 | 說明 |
|---|---|---|---|
| ← Catalog | Catalog Context | Customer / Supplier(下游) | 加入購物車時讀 Catalog,記 `ProductRef`。訂閱 `Catalog.PriceChanged`、`Catalog.ProductUnpublished` 來 revalidate cart。 |
| → Order | Order Context | Customer / Supplier(上游) | 客人按下結帳成功時,Cart 發 `CartCheckedOut` 事件,Order 訂閱並建立 Order。Cart 自己轉為 `CheckedOut` 終局狀態。 |

### 關鍵不變量
詳見 `invariants.md`(CART-INV-*)。

---

## BC-3:Order Context(訂單)— 核心域

### 用途
擁有**客人與商家之間的商業交易**:訂單聚合、定價凍結、訂單生命週期。這裡是錢真正準備被賺到的地方 — 其他兩個 BC 都是為了餵養這個。

### 戰略定位
- **類型**:核心域 (Core Domain)
- **變動性**:高 — 訂單規則、定價邏輯、業務流程會隨業務演進。
- **投資原則**:要自己蓋,不要外包。最厚的領域模型、最嚴的測試、最仔細的不變量。

### 邊界

**邊界內**
- Order Aggregate(下單後的持久商業承諾)
- OrderLine、OrderLineSnapshot(完整快照,含定價)
- Order 狀態機(見 `invariants.md` ORD-INV-001)
- 結帳當下的訂單定價(計算一次,凍結)
- 訂單取消、訂單完成等狀態轉移

**明確排除**
- 商品主資料 → 屬於 Catalog(Order 持有的是**快照**)
- 購物車內容 → 屬於 Cart(Order 在收到 `CartCheckedOut` 時從事件 payload 拿到一份,之後 Cart 跟 Order 互不影響)
- 庫存扣減、付款、出貨 → v1 範圍外,但已預留在狀態機裡(`PendingPayment`、`Paid`、`Shipped`)
- 通知、發票 → 由訂閱 Order 事件的下游處理

### Ubiquitous Language(節錄)
**Order**、**OrderLine**、**OrderLineSnapshot**、**PlaceOrder**、**OrderStatus**、**Subtotal**、**Total**。

> ⚠️ Order 裡的「Product」也是快照 — `OrderLineSnapshot`,跟 Catalog 的 `Product`、Cart 的 `ProductRef` 都是不同型別。

### 整合方式

| 方向 | 對象 | 模式 | 說明 |
|---|---|---|---|
| ← Cart | Cart Context | Customer / Supplier(下游) | 訂閱 `CartCheckedOut`,從 payload 拿到 cart 內容後**自行**重新讀 Catalog 取完整快照、算總額、建 Order。 |
| ← Catalog | Catalog Context | Customer / Supplier(下游)+ ACL | 結帳當下讀 Catalog **一次**,取完整快照。ACL 把 `Catalog.Product` 轉成 `Order.OrderLineSnapshot`,定義屬於 Order 自己的命名與結構。 |
| → 下游 | (未來的 Payment / Inventory / Notification 等) | Published Language | 發布 `Order.Placed`、`Order.Cancelled`、`Order.Completed` 等。 |

### 關鍵不變量
詳見 `invariants.md`(ORD-INV-*)。

---

## 商品流經三個 BC 時的變形

同一個商品在三個 BC 裡有**三個不同型別**,設計上是刻意的。看一遍它的人生:

```
[Catalog]              [Cart]                  [Order]
Product       ──►    ProductRef       ──►    OrderLineSnapshot
(完整、可變)          (輕量、會 revalidate)    (完整、凍結)
─────────────         ─────────────────        ──────────────────
productId             productId                 productId
sku                   sku                       sku
name                  name                      name
description           thumbnailUrl              unitPrice  ◄── 凍結
attributes[]          currentPrice  ◄ 會更新    quantity
media[]                                         lineTotal
listPrice  ◄ 可變                               
status                                          
categoryIds[]                                   
```

幾個重點:

- **Cart 的 `ProductRef`** 故意只放 UI 顯示需要的最小資訊,加一個會 revalidate 的 `currentPrice`。Catalog 改價時 Cart 知道、會更新並通知客人。
- **Order 的 `OrderLineSnapshot`** 在下單那一刻取得完整版,**永遠凍結**。Catalog 之後改名、改價、下架,Order 都不為所動。
- 兩個快照各有設計目的,**不能**互相替代。把 `ProductRef` 直接塞進 Order 是 bug,因為它沒有凍結的 `unitPrice`。

---

## 三個 BC 之間的事件流

### Happy path:從瀏覽到下單成功

```
客人瀏覽
  │
  ├─► AddToCart ──► Cart Context
  │                   │
  │                   └─► (內部) Cart 讀 Catalog,記 ProductRef
  │
  ├─► UpdateQuantity / RemoveFromCart ──► Cart Context (高頻)
  │
  └─► Checkout ──► Cart Context
                     │
                     ├─► 驗證 cart 不為空、所有 ProductRef 仍可賣
                     ├─► 發出 CartCheckedOut 事件
                     └─► Cart 自身轉為 CheckedOut 終局狀態

                     CartCheckedOut
                          │
                          ▼
                   Order Context (訂閱)
                          │
                          ├─► 從 Catalog 讀完整快照
                          ├─► 算 Subtotal / Total
                          ├─► 建立 Order(status = PendingPayment)
                          └─► 發 Order.Placed
```

### Sad path:Cart 失效(Catalog 改了)

```
Catalog 改 Product 價格 ──► Catalog.PriceChanged 事件
                                       │
                                       ▼
                                 Cart Context (訂閱)
                                       │
                                       └─► 找出所有持有此 productId 的 active cart
                                           ├─► 標記 cart 為 NeedsRevalidation
                                           └─► 下次客人開啟 cart 時 UI 顯示警示

客人按下 Checkout 但 cart 有 NeedsRevalidation 標記
  │
  └─► Cart 拒絕 checkout,要求客人確認新價格
      → 客人確認 → 清掉標記、繼續 checkout
      → 客人不確認 → cart 保留,checkout 取消
```

---

## 跨 Context 共通規則

以下規則適用於**每一條** BC 邊界。

1. **不共用資料庫。** 每個 BC 自管自的儲存(Catalog 可用 RDBMS+ES,Cart 適合 Redis,Order 用 RDBMS)。跨 BC 讀取只透過明確的 API 或事件。
2. **不跨 BC 共用領域型別。** Catalog 的 `Product`、Cart 的 `ProductRef`、Order 的 `OrderLineSnapshot` 是三個獨立型別。轉換發生在邊界。
3. **事件用過去式、用發布者的語言命名。** `Cart.CheckedOut`,不是 `OnCheckOut`。發布的事件是 BC 的公開契約 — 見 `domain-events.md`。
4. **命令用意圖式語言,並指向特定 BC。** `AddToCart`、`PlaceOrder`。
5. **快照永遠優於參照。** 任何跨 BC 需要保留資訊的場合,複製過去後就脫鉤。Order 不會在事後再去問 Catalog「這個商品還在嗎」。
6. **BC 之間最終一致、BC 內部強一致。** Aggregate 內部用 transaction 保證一致,BC 之間靠事件與訂閱收斂。
7. **冪等性是預設要求。** 每個跨 BC 事件帶 `eventId`,訂閱方靠它去重。

---

## 後續路線圖

當下需要的就是這三個 BC。隨著功能擴展,以下會獨立出來:

| 何時擴展 | 新增 BC | 切入點 |
|---|---|---|
| 接入金流(信用卡、第三方支付) | Payment Context | Order 發 `Order.Placed` 後 Payment 訂閱並請款 |
| 處理庫存超賣問題 | Inventory Context | 在 Cart.checkout 與 Order.placed 之間插入 ReserveStock |
| 客戶資料超出 `customerId` 一個 UUID | Customer / Identity Context | 從 Order、Cart 各自拉出來 |
| 促銷與優惠券 | Promotion Context | 在 Cart 顯示折後價、在 Order 凍結套用後的折扣 |
