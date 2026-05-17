# Agent Workflow — DDD + TDD 購物商城開發

> **適用工具**：Claude Code CLI
> **Plugin**：`ddd@context-engineering-kit`（規則自動附加到 context）
> **Skills**：`ecc:tdd-workflow`
> **DDD 文件**：`docs/domain/{bounded-contexts,ubiquitous-language,invariants,domain-events}.md`

本文件為**主索引**。各 Phase 的詳細範本與檢查清單拆分至 `workflow/` 子目錄，agent 進入對應 Phase 時才需載入。

---

## 文件導覽

| 檔案 | 何時讀取 |
|---|---|
| `workflow/ddd-rules-reference.md` | 任何 Phase 需查 DDD Rule 細節時 |
| `workflow/phase-2-red-templates.md` | Phase 2 撰寫測試前 |
| `workflow/phase-3-green-templates.md` | Phase 3 實作 domain 前 |
| `workflow/phase-4-refactor-checklist.md` | Phase 4 重構前 |
| `workflow/phase-5-application-templates.md` | Phase 5 實作 Application Service 前 |
| `workflow/phase-6-adapter-templates.md` | Phase 6 實作 Controller / JPA 前 |
| `workflow/phase-7-acceptance.md` | Phase 7 驗收時 |

---

## 全流程總覽

```
Phase 0  DDD 分析       → 讀取 4 份 DDD 文件，產出分析摘要
Phase 1  領域設計       → 設計 Aggregate / Port（不寫程式碼）
Phase 2  🔴 Red         → 先寫測試（必須 FAIL）
Phase 3  🟢 Green       → 實作 Domain 通過測試
Phase 4  🔵 Refactor    → 重構 + DDD rules 合規
Phase 5  Application    → Use Case 完整實作
Phase 6  Adapter        → Controller + JPA
Phase 7  測試驗收       → 全套測試 + Coverage
Phase 8  開發報告       → 產出 Report
```

每個 Phase 結束建立 Git checkpoint（commit prefix 見附錄）。

---

## Phase 0：DDD 分析

> **目標**：從 4 份 DDD 文件萃取本次開發 BC 的所有設計約束。

### 讀取順序

1. `bounded-contexts.md` → BC 邊界、Aggregate、Port 清單
2. `ubiquitous-language.md` → 術語對照（命名必須一致）
3. `invariants.md` → 不變量清單
4. `domain-events.md` → 發布/訂閱事件清單

### Gate G0（全通過才進 Phase 1）

| # | 項目 | 通過條件 |
|---|---|---|
| G0-1 | Bounded Context | 清楚知道本 BC 的 Aggregate Root、Port In/Out |
| G0-2 | Ubiquitous Language | 命名已對照詞彙表 |
| G0-3 | Invariant | 不變量清單列出，對應 Exception 名稱確認 |
| G0-4 | Domain Event | 發布/訂閱事件與 schema 欄位確認 |

### 輸出物範本

```markdown
## [BC 名稱] 分析摘要

### Aggregate
- Aggregate Root: XXX
- Entities: [...]
- Value Objects: [...]

### Invariants（本 BC）
- [BC]-1: [規則描述] → 違反拋 [ExceptionName]

### Domain Events
- 發布: [EventName]
- 訂閱: [EventName]

### Port 清單
- In: [UseCaseName]
- Out: [RepositoryName]
```

---

## Phase 1：領域設計

> **目標**：決定程式碼結構，不寫任何實作。

### Package 結構

```
com.example.claudecodeclidemo.<bc>/
├── domain/
│   ├── entity/      ← Aggregate Root, Entity
│   ├── vo/          ← Value Object（不可變）
│   ├── event/       ← Domain Event（過去式命名）
│   └── exception/   ← Domain Exception（對應 Invariant）
├── application/port/
│   ├── in/          ← Use Case Interface
│   └── out/         ← Repository / External Port Interface
└── adapter/
    ├── in/web/      ← Controller + DTO
    └── out/persistence/  ← JPA Entity + Repository Impl
```

### 設計核心檢查（重構期完整清單見 `workflow/phase-4-refactor-checklist.md`）

- [ ] Domain 層無 `import org.springframework.*`、`import jakarta.persistence.*`
- [ ] Value Object 不可變（`final` fields），實作 `equals()` / `hashCode()`
- [ ] Aggregate Root 是唯一修改內部狀態的入口
- [ ] Port Interface 位於 `application/port/`，以 `UseCase` 或 `Port` 結尾
- [ ] Domain Event 為不可變 record / class，含 `occurredAt: Instant`

---

## Phase 2：🔴 Red

> **Skill**：`ecc:tdd-workflow`。**未確認 RED 前禁止修改 production code。**
> **範本**：`workflow/phase-2-red-templates.md`

撰寫順序：VO → Entity 不變量 → Entity 狀態轉換 → Domain Event 發布 → Use Case（Mock Port Out）。

**RED Gate**：`./gradlew test` 確認測試全部 FAIL，失敗原因為「實作不存在」（非 syntax error）。

---

## Phase 3：🟢 Green

> **DDD Rule**：`functional-core-imperative-shell`
> **範圍**：Domain 層 + Port Interface。Application Service 完整實作保留至 Phase 5。
> **範本**：`workflow/phase-3-green-templates.md`

實作順序：Value Objects → Domain Exceptions → Entities/Aggregate Root → Domain Events → Port Interfaces。

**GREEN Gate**：`./gradlew test` 確認所有測試 PASS。

---

## Phase 4：🔵 Refactor

> **完整檢查清單**：`workflow/phase-4-refactor-checklist.md`

涵蓋命名、函式設計、資料流、錯誤處理、控制流程、CQS。

**Refactor Gate**：`./gradlew test` 測試必須仍全綠。

---

## Phase 5：Application Layer

> Phase 3 僅建立 Port Interface；此 Phase 完整實作 Application Service，加上 Spring 事務邊界、事件發布與測試。
> **範本**：`workflow/phase-5-application-templates.md`

核心守則：
- Service **不含業務邏輯**（邏輯在 domain）
- Service **不直接操作 DB**（透過 Port Out）
- 事務邊界在此層（`@Transactional`）
- 一個 Use Case = 一個公開方法

---

## Phase 6：Adapter Layer

> **DDD Rule**：`library-first-approach`、`clean-architecture-ddd`、`separation-of-concerns`
> **範本**：`workflow/phase-6-adapter-templates.md`

- **Adapter In**：`@RestController` 薄薄一層，只做 DTO ↔ Command/Query 轉換
- **Adapter Out**：JPA Entity 與 Domain Entity 完全分離（JPA annotation 只出現在 `adapter/out/persistence/`）

---

## Phase 7：測試驗收

> **詳細指令與 Gate**：`workflow/phase-7-acceptance.md`

執行 `./gradlew test jacocoTestReport`，確認 6 項驗收 Gate（V-1 ~ V-6）與 Coverage 達標（domain ≥ 90%、整體 ≥ 60%）。

---

## Phase 8：開發報告

產出至 `docs/dev-reports/<bc-name>-report.md`。

```markdown
# [BC 名稱] 開發報告

**日期**：YYYY-MM-DD
**Branch**：feature/<bc-name>

## 實作摘要
- Aggregate 清單（Class | 類型 | 說明）
- Use Case 清單（Interface | 實作 | 說明）
- Domain Event 清單（Event | 觸發時機 | 消費者）

## TDD 循環記錄
| 循環 | 測試案例 | RED | GREEN | REFACTOR |

## 覆蓋率
| 層次 | Line | Branch |

## DDD Rules 合規結果
| Rule | 狀態 | 備註 |

## 已知限制與待辦
```

---

## 附錄：Skill 呼叫時機

| 時機 | Skill / Plugin |
|---|---|
| 進入任何 BC 開發 | `ddd` plugin 自動生效 |
| Phase 2 ~ 4（TDD 循環） | `/ecc:tdd-workflow` |
| 程式碼 review | `/ecc:code-review` |
| 安全性檢查 | `/ecc:security-review` |
| Coverage 不足修補 | `/ecc:test-coverage` |

## 附錄：Git Commit 慣例

| 階段 | Prefix | 範例 |
|---|---|---|
| Red | `test:` | `test: add red tests for Order invariants` |
| Green | `feat:` | `feat: implement Order domain layer` |
| Refactor | `refactor:` | `refactor: clean up Order domain layer` |
| Application | `feat:` | `feat: implement CreateOrderService` |
| Adapter | `feat:` | `feat: implement Order REST adapter and JPA adapter` |
| Fix | `fix:` | `fix: handle empty cart in checkout` |
