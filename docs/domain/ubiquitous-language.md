# Ubiquitous Language(統一語言)

> 領域專家、開發者、利害關係人共用的詞彙。
> 詞彙表中的每個術語都會**原封不動地出現在程式碼、對話與文件中**。
> 同一個字在不同 BC 有不同意義時,兩種意義都要記錄,並明確標出 BC 範圍。

---

## 怎麼讀這份文件

每一筆條目包含:

- **術語** — 在程式碼與口語中使用的精確字詞。
- **類型** — Aggregate / Entity / Value Object / Domain Service / Event / Command / Policy / Process。
- **定義** — 一兩句話的精確意義。

> ⚠️ **多義標記(`⚠️`)代表這個字在不同 BC 看起來一樣但意思不同。**
> 這是 bug 與溝通誤會的最大來源,請仔細讀。

---

## 跨 BC 共用概念

| 術語 | 類型 | 定義 |
|---|---|---|
| **Bounded Context (BC)** | 戰略概念 | 在邊界內模型與語言一致的語意範圍。跨出去後同一個字可能完全不同意。 |
| **Aggregate** | 戰術模式 | 一組被視為單一單位變更的物件群。有一個 **Aggregate Root** 作為所有存取的入口。 |
| **Aggregate Root** | 戰術模式 | Aggregate 的唯一入口 entity。負責守護 aggregate 的 invariants。 |
| **Entity** | 戰術模式 | 具有 ID 並隨時間延續身份的物件。 |
| **Value Object** | 戰術模式 | 只由屬性定義的物件;值相等 = 物件相等。不可變。例:`Money`、`SKU`、`ProductRef`、`OrderLineSnapshot`。 |
| **Domain Event** | 戰術模式 | 領域中某件事已經發生的事實,以過去式命名(如 `Cart.CheckedOut`、`Order.Placed`)。 |
| **Command** | 戰術模式 | 對 aggregate 提出的狀態改變意圖。命令式(如 `AddToCart`、`PlaceOrder`)。 |
| **Snapshot** | 概念 | 跨 BC 邊界時把資料**複製過去**而非保留參照的設計。複製後與來源脫鉤。本系統有兩種快照:`ProductRef`(Cart 用)、`OrderLineSnapshot`(Order 用)。 |
| **Anti-Corruption Layer (ACL)** | 整合模式 | 位於 BC 邊界的翻譯層,防止其他 BC 的模型汙染我們的模型。 |
| **Money** | Value Object | 一對 `{amount: Decimal, currency: ISO-4217}`。算術只能在同幣別內進行。**絕不**使用 `float`。 |
| **SKU** | Value Object | Stock Keeping Unit。對客人可見、穩定的商品識別字串。在 Catalog 內全域唯一,跨 BC 引用時用它。 |
| **Idempotency Key** | Value Object | 對同一操作出現兩次時,確保操作至多執行一次、且結果相同的識別碼。 |

---

## Catalog Context

| 術語 | 類型 | 定義 |
|---|---|---|
| **Product** ⚠️ | Aggregate Root | 在我們目錄中存在的可賣商品:身份、文案、媒體、分類、標價、生命週期狀態。<br>⚠️ 跟 Cart 的 `ProductRef`、Order 的 `OrderLineSnapshot` 是不同型別。 |
| **ProductId** | Value Object | Catalog 對商品的內部 UUID。穩定識別,跨 BC 引用都用這個。 |
| **Draft** | Status | 商品的初始狀態;對客人不可見、不可賣。 |
| **Published** | Status | 商品對客人可見、開放販售。 |
| **Archived** | Status | 商品已下架且不可變。歷史訂單仍可參照其快照。 |
| **Category** | Entity | 商品的分類群。一個商品可同時屬於多個 Category。 |
| **Attribute** | Value Object | 商品的具名特徵(如 `color = "navy"`、`size = "M"`)。 |
| **ListPrice** ⚠️ | Value Object | 商品目前掛出的標價,型別是 `Money`。<br>⚠️ Cart 看到的 `currentPrice` 取自此處但會 revalidate;Order 的 `unitPrice` 取自此處但凍結。 |
| **Publish** | Command | 狀態轉移 `Draft → Published`。要符合 CAT-INV-002。 |
| **Archive** | Command | 狀態轉移 `Published → Archived`。不可逆。 |
| **UpdatePrice** | Command | 改變 `ListPrice`。發 `Catalog.PriceChanged` 事件。 |

---

## Cart Context

| 術語 | 類型 | 定義 |
|---|---|---|
| **Cart** | Aggregate Root | 客人正在考慮中的暫時商品集合。沒有商業承諾,可隨意棄置。每個客人(或訪客 session)同時只有一個 active Cart。 |
| **CartId** | Value Object | Cart 的內部識別碼。可能對應到 customerId(會員)或匿名 sessionId(訪客)。 |
| **CartItem** | Entity(屬於 Cart) | 車內一行:一個 `ProductRef` 加一個 `quantity`。身份在 Cart 內局部。 |
| **ProductRef** ⚠️ | Value Object | Cart 從 Catalog 拿到的**輕量快照**:`{productId, sku, name, thumbnailUrl, currentPrice}`。<br>⚠️ 設計上是「會 revalidate 的快取」 — 當 `Catalog.PriceChanged` 來時會更新 `currentPrice`。**不是**凍結快照,**不能**當 Order 用。 |
| **CartStatus** | Enum(狀態機) | `Active`、`CheckedOut`、`Abandoned`、`Expired` 其中之一。詳見 CART-INV-001。 |
| **AddToCart** | Command | 把某 productId × quantity 加入 cart。會去讀 Catalog 取 ProductRef。 |
| **UpdateQuantity** | Command | 改變 CartItem 的數量。`quantity = 0` 視同 `RemoveFromCart`。 |
| **RemoveFromCart** | Command | 從 cart 移除某個 CartItem。 |
| **Checkout** ⚠️ | Command | 客人按下結帳。Cart 驗證後發 `CartCheckedOut` 事件,自身轉為 `CheckedOut` 終局狀態。<br>⚠️ Checkout **不直接建立 Order** — 它只發事件,Order Context 訂閱後才建。 |
| **Abandon** | Command | 客人或系統明確放棄 cart。轉為 `Abandoned` 終局狀態。 |
| **Revalidate** | Process | 當訂閱到 Catalog 事件時,Cart 重新檢查每個 ProductRef 是否仍有效並更新 `currentPrice`。 |
| **NeedsRevalidation** | 標記 | Cart 上的旗標,表示有 ProductRef 已變動,checkout 前需客人確認。 |
| **Merge** | Process | 訪客 cart 與會員 cart 在登入時的合併規則(數量相加,衝突則取較大值)。 |
| **EstimatedTotal** ⚠️ | Value Object | Cart 顯示的總額,只用於 UI 提示。<br>⚠️ 是**估算**,不是承諾。實際成交金額由 Order 在結帳時計算並凍結。 |

> ⚠️ Cart 不講「Subtotal」「Total」這些字 — 這兩個名詞屬於 Order,代表凍結的承諾金額。Cart 只說 **EstimatedTotal**,以明確區分。

---

## Order Context(核心域)

| 術語 | 類型 | 定義 |
|---|---|---|
| **Order** | Aggregate Root | 一個商業承諾:已確認的明細、已確認的金額、客戶、生命週期。已下單後不可悄悄丟棄。 |
| **OrderId** | Value Object | Order 的內部識別碼,給客人看的「訂單編號」(可能另外生一個對外的可讀字串)。 |
| **OrderLine** | Entity(屬於 Order) | 訂單中的一行:一個 `OrderLineSnapshot` 加一個 `quantity` 與計算好的 `lineTotal`。 |
| **OrderLineSnapshot** ⚠️ | Value Object | 在 `PlaceOrder` 當下從 Catalog 複製的**完整凍結副本**:`{productId, sku, name, unitPrice}`。<br>⚠️ 跟 Catalog 的 `Product`、Cart 的 `ProductRef` 都不一樣。**永遠不變**。 |
| **PlaceOrder** | Command | 訂閱 `CartCheckedOut` 後內部執行的命令,實際把 `Order` aggregate 建出來。**不是**客人直接呼叫的 API。 |
| **OrderStatus** | Enum(狀態機) | `PendingPayment`、`Paid`、`Shipped`、`Completed`、`Cancelled` 其中之一。詳見 ORD-INV-001。 |
| **CancelOrder** | Command | 把訂單轉到 `Cancelled`。未來會觸發 saga 補償(目前 v1 只是狀態改變)。 |
| **MarkAsPaid** | Command | 收到付款確認後把訂單轉到 `Paid`。v1 由人工或外部對帳觸發,未來由 Payment Context 訂閱事件觸發。 |
| **Subtotal** | Value Object | 所有明細 `lineTotal` 加總,稅前運費前折扣前。**凍結**。 |
| **Total** | Value Object | 客人最終支付的金額。在 `PlaceOrder` 凍結。v1 = Subtotal(尚未支援稅、運費、折扣)。 |
| **PlacedAt** | Timestamp | Order 建立的時間。Order 唯一沒有對應狀態的時間欄位,代表「商業承諾誕生的瞬間」。 |

---

## 多義索引(同字異義對照)

這張表是這份文件**最重要**的部分。寫程式或開會時,遇到下面任一個字,先想清楚是在哪個 BC。

| 字 | 在 Catalog | 在 Cart | 在 Order |
|---|---|---|---|
| **Product** ⚠️ | 完整的 `Product` Aggregate Root | 一個 `ProductRef`(輕量快照,會 revalidate) | 一個 `OrderLineSnapshot`(完整快照,凍結) |
| **Price** ⚠️ | `ListPrice` — 當前的、可變的 | `ProductRef.currentPrice` — 會 revalidate 的顯示值 | `OrderLineSnapshot.unitPrice` — 下單當下凍結 |
| **Status** | 商品生命週期(`Draft`、`Published`、`Archived`) | Cart 生命週期(`Active`、`CheckedOut`…) | 訂單生命週期(`PendingPayment`、`Paid`…) |
| **Total** ⚠️ | 不存在 | **不講** Total,只講 `EstimatedTotal` | `Total` — 凍結的承諾金額 |
| **Checkout** ⚠️ | 不存在 | 命令:客人按下結帳,Cart 發事件 | (不是 Order 的詞,Order 收到事件後執行 `PlaceOrder`) |
| **Cancel** | (不用 — 商品用 `Archived`) | (不用 — cart 用 `Abandoned` 或 `Expired`) | 命令:`CancelOrder` |

寫程式碼或文件時,**遇到有歧義的字一律加 BC 前綴**:寫 `Catalog.Product`、`Cart.ProductRef`、`Order.OrderLineSnapshot`,而不是「商品」。

---

## 商品三型別速覽

```
┌─────────────────────┬─────────────────────┬─────────────────────┐
│  Catalog.Product    │  Cart.ProductRef    │  Order.             │
│                     │                     │  OrderLineSnapshot  │
├─────────────────────┼─────────────────────┼─────────────────────┤
│ Aggregate Root      │ Value Object        │ Value Object        │
│ 可變                │ 部分可變(revalidate)│ 不可變(凍結)        │
│ 完整                │ 輕量(UI 用)         │ 完整(含 unitPrice) │
│ Catalog 擁有        │ Cart 擁有快照        │ Order 擁有快照       │
│ 全域唯一            │ Cart 內局部          │ Order 內局部         │
└─────────────────────┴─────────────────────┴─────────────────────┘
```

---

## 禁用詞(不准用)

這些字在真實對話中常常出現,但**不准進入程式碼或正式文件**。它們含糊、有歧義、或洩漏抽象。

| 禁用詞 | 為什麼禁 | 改用 |
|---|---|---|
| **Item** | 講者可能想表達 Product、CartItem、OrderLine 任一種,沒人知道。 | 挑一個:`Product`、`CartItem`、`OrderLine`、`ProductRef`、`OrderLineSnapshot`。 |
| **「車裡有 5 個商品」** | 商品是哪個 BC 的?5 是行數還是總數? | 「Cart 有 5 個 CartItem」或「Cart 共有 12 個 quantity」。 |
| **「下單」** | 是 `Checkout`(Cart 的命令)還是 `PlaceOrder`(Order 內部)? | 講確切:客人「checkout」,Order「place」。 |
| **Process**(模糊) | 幾乎一定講得不夠清楚。 | 點名:「checkout 流程」、「Cart revalidation」。 |
| **「訂單處理完了」** | 處理是什麼?成立?付款?出貨? | 用確切狀態:「Order is `Paid`」「Order is `Completed`」。 |
| **「商品價格」**(無 BC 前綴) | Catalog 的 ListPrice?Cart 的 currentPrice?Order 的 unitPrice? | 一定加前綴:`Catalog.ListPrice`、`Cart.currentPrice`、`Order.unitPrice`。 |
| **「購物車變訂單」** | Cart **不會變成** Order。Cart 自己進終局狀態 `CheckedOut`,Order 是另一個新建的 aggregate。 | 「客人 checkout cart 後,Order Context 建立一張新的 Order」。 |

---

## 在對話中正確使用這套語言

正確的句子長這樣:

> 「客人 **AddToCart** 一個 SKU=ABC-123 的 product。Cart 從 Catalog 讀回 `ProductRef`,放進 `CartItem`。之後 Catalog 改了 `ListPrice`,Cart 收到 `Catalog.PriceChanged` 後標記 `NeedsRevalidation`。客人按下 **Checkout**,確認新價後 Cart 發 **Cart.CheckedOut** 事件並轉為 `CheckedOut` 終局狀態。Order Context 訂閱事件,執行 **PlaceOrder**,建立一張 Order(status = `PendingPayment`),每行存 `OrderLineSnapshot` 把 `unitPrice` 凍結,發 **Order.Placed**。」

錯誤的句子長這樣:

> 「使用者把商品加入購物車,然後下單,訂單就成立了。」

第二句完全沒有 BC 邊界感,「商品」「下單」「成立」每個字都可以指不同的東西。

---

## 維護方式

- 新增術語:用 PR 同時新增條目與程式碼中第一個使用點。兩者必須一致。
- 改名:同一個 PR 內把程式碼、本文件、以及**所有其他 DDD 文件**全部改掉。Ubiquitous Language 是「無所不在」的;部分改名是禁止的。
- 移除:標 `[RETIRED]` 而不是直接刪,以便歷史 PR 與事故報告仍可搜尋。
