# Catalog BC 開發報告

**日期**: 2026-05-14  
**開發者**: Claude Sonnet 4.6 (DDD+TDD Agent)  
**方法論**: Domain-Driven Design + Test-Driven Development

---

## 實作摘要

### Aggregate

| Aggregate Root | Entities | Value Objects |
|---|---|---|
| Product | — | ProductId, Sku, Money, CategoryId |
| Stock | — | ProductId, Sku |

### Use Cases

| Interface | 實作 | 說明 |
|---|---|---|
| `CreateProductUseCase` | `ProductService` | 建立商品 + 初始化庫存 |
| `QueryProductUseCase` | `ProductService` | 查詢商品 |
| `CatalogQueryPort` | `StockService` | 查詢價格 / 活躍狀態 |
| `StockReservationPort` | `StockService` | 保留 / 釋放 / 扣除庫存 |

### Domain Events

| 事件 | 觸發時機 | 消費者（v2） |
|---|---|---|
| `ProductCreatedEvent` | `Product.create()` | — |
| `ProductPriceChangedEvent` | `Product.changePrice()` | Cart BC |
| `StockDepletedEvent` | `Stock.deduct()` 後 quantity = 0 | Catalog（自動下架）|

### REST Endpoints

| Method | Path | 說明 |
|---|---|---|
| POST | `/api/products` | 建立商品（201 Created）|
| GET | `/api/products/{id}` | 查詢商品（200 OK）|

---

## TDD 循環記錄

| Phase | 說明 | Commit |
|---|---|---|
| RED | 撰寫 VO / Entity / Event / UseCase 測試（全部 FAIL） | `789ee96` |
| GREEN | 實作 Domain + Application 層（69 tests PASS） | `42edc97` |
| REFACTOR | 修正 Stock.sku null bug、移除 setSku()、命名合規 | `e086148` |
| ADAPTER | Adapter 層（JPA + REST）+ @Transactional | `e7ae193` |
| COVERAGE | 補充 VO 邊界測試，domain.vo 73% → 99% | `a29ef95` |

---

## 覆蓋率（最終）

| 套件 | Instructions |
|---|---|
| `domain.entity` | 100% |
| `domain.event` | 100% |
| `domain.exception` | 100% |
| `domain.vo` | 99% |
| `application.service` | 93% |
| `adapter.out.persistence` | 100% |
| `adapter.in.web` | 86% |
| **整體** | **96%** |

---

## DDD Rules 合規結果（14 條）

| # | 規則 | 結果 |
|---|---|---|
| 1 | domain-specific-naming（無 Utils/Helper/Manager） | PASS |
| 2 | ubiquitous-language（命名符合詞彙表） | PASS |
| 3 | aggregate-root-pattern（Product/Stock 繼承 AggregateRoot） | PASS |
| 4 | value-object-immutability（Money/Sku/ProductId/CategoryId final）| PASS |
| 5 | domain-event-pattern（immutable record + occurredAt） | PASS |
| 6 | domain-purity（domain 層零 Spring/JPA import） | PASS |
| 7 | hexagonal-architecture（Domain → Application → Adapter 單向依賴） | PASS |
| 8 | port-adapter-separation（Port 介面在 application/port/）| PASS |
| 9 | invariant-enforcement（guard clause 在 Aggregate 方法內）| PASS |
| 10 | command-query-separation（命令無回傳，查詢無副作用）| PASS |
| 11 | early-return-pattern（錯誤路徑優先 throw）| PASS |
| 12 | function-file-size-limits（方法 ≤ 20 行，類別 ≤ 200 行）| PASS |
| 13 | explicit-control-flow（無隱式 null 傳遞）| PASS |
| 14 | reconstitute-factory（JPA ↔ Domain 分離，透過 reconstitute()）| PASS |

---

## 驗收 Gate 結果（V-1 ～ V-6）

| Gate | 門檻 | 實際 | 結果 |
|---|---|---|---|
| V-1 | 0 failures | 86 tests, 0 failures | ✅ |
| V-2 整體 | ≥ 60% line | 96% instructions | ✅ |
| V-2 domain | ≥ 90% | entity/event/exception 100%, vo 99% | ✅ |
| V-3 domain 純度 | 無 Spring/JPA | grep 確認無 import | ✅ |
| V-4 命名合規 | ubiquitous-language | 人工審查通過 | ✅ |
| V-5 Invariant 測試 | P-1～P-6, S-1～S-5 各有負向測試 | 11 條 invariant × 1+ 負向 | ✅ |
| V-6 Event 測試 | 每個 Event 有發布驗證 | 3 Events 各有 isA() verify | ✅ |

---

## 已知限制與後續待辦

1. **Spring Boot 4.0.6 特異**：`@WebMvcTest` / `@DataJpaTest` 切片測試已移除，改用 `@SpringBootTest`；Web 層測試啟動完整 context，執行較慢。
2. **CategoryExistsAdapter**：目前直接查 JPA，Category 尚無獨立管理 Use Case（v2 待補）。
3. **StockDepletedEvent 消費者**：Catalog 自動下架邏輯尚未實作（v2 待補）。
4. **UpdateProductUseCase**：`ProductPriceChangedEvent` 觸發者尚未建立 API 端點（v2 待補）。
5. **`@Transactional` on readOnly**：`StockService.isActive()` 尚未加 readOnly，可最佳化。
