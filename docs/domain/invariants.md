# Invariants(不變量)

> 每個 Bounded Context 內**任何時刻都必須成立**的業務規則。
> 每條 invariant 都有唯一識別、可測試、由特定層級(Aggregate / Domain Service / Saga)強制。
> 如果一條規則無法用程式碼表達,它就不該寫在這裡 — 改寫成文件即可。

---

## 命名與規範

### ID 格式
`<BC>-INV-<NNN>` — 穩定識別碼,永不回收、永不重用。退休的 invariant 仍保留在文件並標 `[RETIRED]`。

### 強制層級
- **Aggregate** — 在 aggregate root 內強制,交易內保證一致。
- **Domain Service** — 同一 BC 內多個 aggregate 一起參與時,由 domain service 強制。
- **Saga / Event Handler** — 跨 BC,透過事件編排與補償達成。**最終一致**。
- **資料庫約束** — 最後一道防線(unique index、check constraint)。領域邏輯仍要先擋下,DB 只是保險網。

### 每條 invariant 應包含
**ID**、**敘述**(用領域語言)、**理由**、**強制方式**、**測試**(怎麼驗證)。

---

## Catalog Context

### CAT-INV-001 — SKU 在 Catalog 內全域唯一
> 任一時刻,一個 SKU 只能對應一個商品。

- **理由**:SKU 是其他 BC(Cart、Order 快照)依賴的穩定識別。重複會讓跨 BC 參照爛掉。
- **強制**:Aggregate(建立時檢查) + DB unique index。
- **測試**:用既有 SKU 建第二個商品要失敗,丟 `DuplicateSkuError`。

### CAT-INV-002 — Product 沒設價就不能 Published
> `Product.status = Published` ⇒ `Product.listPrice` 非 null 且 `listPrice.amount > 0`。

- **理由**:沒價格的可賣商品會讓 Cart 顯示空白、Order 無從計算 — 整條鏈會壞。
- **強制**:Aggregate — `Draft → Published` 轉移時檢查。
- **測試**:沒設價就呼叫 `product.publish()` 要丟 `MissingPriceError`。

### CAT-INV-003 — 同一 Product 內價格幣別一致
> 同一個商品上的所有價格(listPrice、未來的促銷價)只能有一個幣別。

- **理由**:同筆商品同時掛 TWD 和 USD 在語意上沒意義。Cart 與 Order 都依賴單一幣別運算。
- **強制**:Aggregate。
- **測試**:設不同幣別的促銷價要丟 `CurrencyMismatchError`。

### CAT-INV-004 — Archived 的商品不可改
> 一旦 `status = Archived`,任何欄位都不准再改。

- **理由**:Archived 是歷史參照用。改了會讓「過去訂單裡的快照」與「現在 Catalog 顯示的版本」不一致,搞壞稽核。
- **強制**:Aggregate — 每個修改方法都先檢查 `status`。

### CAT-INV-005 — 改價必須發出 `Catalog.PriceChanged` 事件
> `UpdatePrice` 命令完成、`ListPrice` 真的變了之後,必須發 `Catalog.PriceChanged` 事件。

- **理由**:Cart 依賴此事件來 revalidate。不發 = Cart 顯示過期價格 → 客人結帳時被嚇到 → 信任受損。
- **強制**:Aggregate + outbox 模式(事件與寫入在同一交易)。
- **測試**:改價後讀 outbox,必有對應事件。

---

## Cart Context

### CART-INV-001 — Cart 狀態必須照狀態機走
> 只允許以下轉移:
>
> ```
> Active ──► CheckedOut    (終局,客人完成 checkout)
>    │
>    ├────► Abandoned      (終局,客人或系統明確放棄)
>    │
>    └────► Expired        (終局,長期未動自動過期)
> ```
> 終局狀態不可逆。客人若要再買,要建一個新的 Cart。

- **理由**:把「正在用的車」「已結帳的車」「死車」分清楚。重用 cart 會讓統計、回報、客服都對不上號。
- **強制**:Aggregate — 每個轉移方法先驗目前狀態。
- **測試**:對 `CheckedOut` 的 cart 呼叫 `addItem()` 要丟 `CartNotActiveError`。

### CART-INV-002 — 每個客人只有一張 Active Cart
> 對任一 customerId(或匿名 sessionId),`status = Active` 的 Cart 至多一張。

- **理由**:多張 active cart 會讓「我的購物車」這個概念失去意義。多裝置同步應該指向同一張。
- **強制**:Aggregate Repository — 找不到時新建,找到 active 時返回;違反由 DB unique index 攔截。
- **測試**:對同一 customerId 連續兩次 `getOrCreateActiveCart()` 要回傳同一個 cartId。

### CART-INV-003 — CartItem 的 quantity 是正整數
> `cartItem.quantity ≥ 1` 且 `cartItem.quantity ∈ ℤ`。
> 若客人 `UpdateQuantity(0)`,等同 `RemoveFromCart`,不會留下 quantity=0 的 CartItem。

- **理由**:`quantity=0` 的行是垃圾資料。
- **強制**:Aggregate — `UpdateQuantity` 內偵測 0 並改走移除路徑。
- **測試**:`updateQuantity(itemId, 0)` 後 cart 內不應存在該 itemId。

### CART-INV-004 — 同一 productId 在 Cart 內只能有一行
> Cart 中不可同時存在兩個 `productId` 相同的 CartItem。

- **理由**:「兩個一樣商品分兩行」對客人沒意義,只會讓 quantity 計算混亂。重複 `AddToCart` 應該是累加 quantity。
- **強制**:Aggregate — `addItem` 內先找既有行,有則累加,無則新建。
- **測試**:對同一 cart 連續 `addItem(productId=X, qty=2)` 兩次,結果應該是一行 quantity=4。

### CART-INV-005 — ProductRef 必須對應一個曾經 Published 的 Product
> CartItem 中的 `ProductRef.productId` 必須是 Catalog 中存在、且加入時為 `Published` 狀態的商品。

- **理由**:不能把 Draft 或 Archived 的商品塞進 cart。
- **強制**:Aggregate — `addItem` 時呼叫 Catalog 的查詢介面,非 Published 則拒絕。
- **測試**:對 Archived 商品 `addItem` 要丟 `ProductNotPurchasableError`。

### CART-INV-006 — NeedsRevalidation 標記下不可 Checkout
> Cart 上若有 `NeedsRevalidation` 標記,`Checkout` 命令必須被拒絕。

- **理由**:revalidation 標記代表「商品價格或可賣性已變動,客人還沒確認」。在這個狀態 checkout 等於用過期資訊建立 Order — 違反客人信任。
- **強制**:Aggregate — `checkout()` 開頭檢查標記。
- **測試**:標記為 `NeedsRevalidation` 後呼叫 `checkout()` 要丟 `CartRequiresRevalidationError`。

### CART-INV-007 — 空 Cart 不可 Checkout
> Cart 中 CartItem 數為 0 時,`Checkout` 命令必須被拒絕。

- **理由**:空訂單沒有商業意義。
- **強制**:Aggregate。

### CART-INV-008 — 收到 `Catalog.PriceChanged` 後必須更新 ProductRef 並設標記
> 對於每張 active Cart,若它含有 PriceChanged 涉及的 `productId`:
> - 更新對應 CartItem 的 `ProductRef.currentPrice` 為新價,且
> - 設置 `NeedsRevalidation = true`。

- **理由**:Cart 是 Catalog 的下游 — 它必須跟上,但**也必須讓客人知道**價格變了。
- **強制**:Event Handler(在 Cart Context 內訂閱 Catalog 事件)。
- **測試**:派發 `Catalog.PriceChanged` 後,所有含此商品的 active cart `currentPrice` 應更新、`NeedsRevalidation` 應為 true。

### CART-INV-009 — Checkout 成功必發 `Cart.CheckedOut` 事件
> Cart 從 `Active → CheckedOut` 的轉移完成後,**必定**發出 `Cart.CheckedOut` 事件,payload 包含完整 cart 內容快照。

- **理由**:這是 Order Context 建立 Order 的唯一觸發點。不發 = Order 永遠不會被建立 = 客人錢花了卻沒訂單。
- **強制**:Aggregate + outbox。
- **測試**:`checkout()` 成功後 outbox 必有對應事件,payload 內 line 數 = cart item 數。

---

## Order Context

### ORD-INV-001 — Order 狀態必須照狀態機走
> 只允許以下轉移:
>
> ```
> PendingPayment → Paid → Shipped → Completed
>         │           │       │
>         └───────────┴───────┴────► Cancelled
> ```
> `Completed` 和 `Cancelled` 是終局狀態。

- **理由**:狀態機是整個商業流程的骨幹。亂跳會搞壞下游所有邏輯(出貨、退款、會計)。
- **強制**:Aggregate — 每個轉移方法先驗目前狀態。
- **測試**:對 `Completed` 的 order 呼叫 `cancel()` 要丟 `InvalidOrderTransitionError`。

### ORD-INV-002 — Order 總金額等於每行小計加總
> `order.subtotal = Σ(line.unitPrice × line.quantity)`
> `order.total = order.subtotal`(v1 尚未支援稅、運費、折扣)
> 在 `PlaceOrder` 時**算一次**並**凍結**。

- **理由**:訂單是某一刻的商業承諾。事後重算就等於價格回溯。
- **強制**:Aggregate — 總額在建構時設好之後不可變。
- **測試**:下單後改 Catalog 價格,重新載入 order,total 仍為原值。

### ORD-INV-003 — OrderLine 的 quantity 是正整數
> `line.quantity ≥ 1` 且 `line.quantity ∈ ℤ`。

- **強制**:Aggregate — OrderLine 建構時驗。

### ORD-INV-004 — OrderLine 只能參照 OrderLineSnapshot,不可直接參照活的 Product
> `line.snapshot` 在 `PlaceOrder` 當下取得後即不可變。Order 不存「以後可能變」的 `Catalog.Product` 參照,也不存 `Cart.ProductRef`(那是 Cart 的型別,不適合做歷史紀錄)。

- **理由**:把訂單歷史跟 Catalog 演進徹底脫鉤。Catalog 改價、改名、下架都不會碰到歷史 order。
- **強制**:Aggregate — `OrderLine` 的 constructor 收 `OrderLineSnapshot`,不做 `productId` 查詢。

### ORD-INV-005 — Order 由 `Cart.CheckedOut` 事件觸發,不可直接從外部 API 建立
> `Order` 的建立只能透過 Order Context 內部訂閱 `Cart.CheckedOut` 事件後執行 `PlaceOrder`。沒有對外的 `createOrder` API。

- **理由**:Cart 是 Order 的唯一上游。允許外部直接建 Order 等於繞過所有 cart 驗證(商品可賣性、客人確認、cart 一致性)。
- **強制**:Application 層 — Order Service 只暴露 event handler,不暴露建立 API。
- **測試**:Order Service 介面中不應有 `placeOrder(...)` 的 public 入口,只有事件訂閱方法。

### ORD-INV-006 — 同一個 CartCheckedOut 事件至多建立一張 Order
> 對任一 `Cart.CheckedOut` 事件(以 `eventId` 識別),Order Context 至多建立一張 Order。

- **理由**:事件可能因網路重試而被送多次。沒有冪等性 = 重複下單 = 客人被重複扣款(未來引入 Payment 後)。
- **強制**:Event Handler 用 `eventId` 做冪等鍵 + DB unique index on `(cartCheckedOutEventId)` 欄位。
- **測試**:同一 eventId 派發兩次,Order 表只有一筆。

### ORD-INV-007 — Order 必須持有 CartCheckedOut 事件的引用
> 每張 Order 都記錄它由哪個 `cartCheckedOutEventId` 建立。

- **理由**:出問題時可追溯(「這張 order 怎麼來的?」)。也是 ORD-INV-006 冪等性的基礎。
- **強制**:Aggregate — 必填欄位。

### ORD-INV-008 — Cancelled Order 不可改任何欄位(除狀態本身的時間戳)
> 一旦 `status = Cancelled`,訂單行、總額、客戶資訊都不可變。

- **理由**:Cancelled 是終局狀態,改它會破壞稽核。
- **強制**:Aggregate。

### ORD-INV-009 — Order 至少包含一個 OrderLine
> `order.lines.length ≥ 1`。

- **理由**:空訂單沒有商業意義。
- **強制**:Aggregate — 建構時驗。配合 CART-INV-007 一起保證(空 cart 不能 checkout)。

---

## 跨 BC Invariants(由 Saga / Event Handler 強制)

這些無法在單一 aggregate 內擋下,只能透過事件編排**最終達成**。

### X-INV-001 — 每個 `Cart.CheckedOut` 最終會對應到一張 Order(或可解釋的失敗)
> 對每個發出的 `Cart.CheckedOut` 事件,5 分鐘內必須出現以下其一:
> - Order Context 建立了一張對應的 Order(以 `cartCheckedOutEventId` 連結),或
> - Order Context 寫入了一筆失敗紀錄,說明為何沒建(例如 Catalog 商品在處理中下架)。

- **強制**:Order Context 的 event handler + 重試 + dead-letter queue。
- **偵測**:對帳 job 每 5 分鐘跑一次,找出有 `Cart.CheckedOut` 但無對應 Order 或失敗紀錄的,告警。

### X-INV-002 — Order.OrderLineSnapshot 反映 PlaceOrder 當下的 Catalog 狀態
> 對每筆 `OrderLine`,`line.snapshot` 等於 PlaceOrder 那一刻 Catalog 中該 productId 的內容。之後 Catalog 的改動不會傳播過來。

- **強制**:Order Context — 快照在 PlaceOrder 內部讀 Catalog 後立即凍結。Persist 之後再也不會讀 Catalog。

### X-INV-003 — Cart 的 EstimatedTotal 與 Order 的 Total 可能不同,且這是合法的
> Cart 顯示的 EstimatedTotal 與最終 Order 的 Total **不保證相等**。差異可能來自:
> - Catalog 在 cart revalidation 與 checkout 之間又改價(極少見但合法)
> - 未來引入稅、運費、折扣後的計算差異

- **強制**:UI 設計上必須在 checkout 確認頁顯示 Order 的最終金額,不能只顯示 cart 的估算。
- **理由**:這不是 bug,是設計。但要明文寫下來,避免有人寫成「cart total = order total」的 assertion。

---

## 反模式(這些不是 invariant)

下面這些常常被誤寫成 invariant,但其實該放別的地方:

- ❌「結帳前要先登入」— 用例的前置條件,屬於 application layer。(訪客 cart 是合法的;會員 cart 是登入後 application 層去合併。)
- ❌「下單時要發送確認信」— 副作用 / 政策,實作成 `Order.Placed` 事件的 handler。
- ❌「Cart 必須在 30 天內過期」— TTL 政策,屬於背景 job 設定,不是領域 invariant。(invariant 是 CART-INV-001 的狀態機 — 過期是允許的轉移之一,但 30 天這個數字不寫死在領域裡。)
- ❌「Order 必須在 Cart checkout 後 1 秒內建立」— 非功能需求,不是 invariant。X-INV-001 講的是「最終會建立」,沒承諾時效。
