# Agent Workflow — DDD + TDD 購物商城開發

> **適用工具**：Claude Code CLI
> **Plugin**：`ddd@context-engineering-kit`（規則自動附加到 context）
> **Skills**：`ecc:tdd-workflow`
> **DDD 文件**：`docs/domain/{bounded-contexts,ubiquitous-language,invariants,domain-events}.md`

本文件為**主索引**。Phase 2~5 的詳細範本與檢查清單拆分至 `workflow/` 子目錄，agent 進入對應 Phase 時才需載入；Phase 0/1/6 因內容精簡，直接寫在本文件。

---

## 文件導覽

| 檔案 | 何時讀取 |
|---|---|
| `workflow/ddd-rules-reference.md` | 任何 Phase 需查 DDD Rule 細節時 |
| `workflow/phase-2-tdd-cycle.md` | Phase 2 TDD 循環（Red / Green / Refactor）前 |
| `workflow/phase-3-application-templates.md` | Phase 3 實作 Application Service 前 |
| `workflow/phase-4-adapter-templates.md` | Phase 4 實作 Controller / JPA 前 |
| `workflow/phase-5-acceptance.md` | Phase 5 驗收時 |

---

## 全流程總覽

```
Phase 0  DDD 分析       → 讀取 4 份 DDD 文件，產出分析摘要
Phase 1  領域設計       → 設計 Aggregate / Port（不寫程式碼）
Phase 2  🔴🟢🔵 TDD 循環 → Red→Green→Refactor per Invariant，Gate 全通過才離開
Phase 3  Application    → Use Case 完整實作
Phase 4  Adapter        → Controller + JPA
Phase 5  測試驗收       → 全套測試 + Coverage
Phase 6  開發報告       → 產出 Report
```

每個 Phase 結束建立 Git checkpoint（commit prefix 見附錄）；**回報進度後等待使用者確認**才繼續。

---

## Phase 0：DDD 分析

> **目標**：從 4 份 DDD 文件萃取本次開發 BC 的所有設計約束。

### 讀取順序

1. `bounded-contexts.md` → BC 邊界、Aggregate、Port 清單
2. `ubiquitous-language.md` → 術語對照（命名必須一致）
3. `invariants.md` → 不變量清單 + Exception 名稱
4. `domain-events.md` → 發布/訂閱事件清單

### Gate G0（全通過才進 Phase 1）

| # | 項目 | 通過條件 |
|---|---|---|
| G0-1 | Bounded Context | 清楚知道本 BC 的 Aggregate Root、Port In/Out |
| G0-2 | Ubiquitous Language | 命名已對照詞彙表 |
| G0-3 | Invariant | 不變量清單列出，對應 Exception 名稱確認 |
| G0-4 | Domain Event | 發布/訂閱事件與 schema 欄位確認 |

### 分析摘要輸出範本

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

> **目標**：決定程式碼結構，列出 class 清單，**不寫任何實作**。
> **DDD Rules**：`clean-architecture-ddd`、`separation-of-concerns`、`domain-specific-naming`、`command-query-separation`、`explicit-side-effects`、`principle-of-least-astonishment`、`explicit-data-flow`

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

### 設計核心檢查（重構期完整清單見 `workflow/phase-2-tdd-cycle.md` § 2c）

- [ ] Domain 層無 `import org.springframework.*`、`import jakarta.persistence.*`
- [ ] Value Object 不可變（`final` fields），實作 `equals()` / `hashCode()`
- [ ] Aggregate Root 是唯一修改內部狀態的入口
- [ ] Port Interface 位於 `application/port/`，以 `UseCase` 或 `Port` 結尾
- [ ] Domain Event 為不可變 record / class，含 `occurredAt: Instant`

### Gate

列出所有將建立的 class（標註型別：Aggregate Root / Entity / VO / UseCase / Port / Event / Exception），等待使用者確認後才進 Phase 2。

---

## Phase 2~5：詳見 workflow/ 子目錄

| Phase | 主題 | 文件 |
|---|---|---|
| 2 | 🔴🟢🔵 TDD 循環（Red→Green→Refactor per Invariant） | `workflow/phase-2-tdd-cycle.md` |
| 3 | Application Service 完整實作 | `workflow/phase-3-application-templates.md` |
| 4 | Adapter In/Out 實作 | `workflow/phase-4-adapter-templates.md` |
| 5 | 全套測試 + Coverage 驗收 | `workflow/phase-5-acceptance.md` |

各 Phase 文件包含：DDD Rules、目標、執行順序、範本程式碼、Gate 指令。本文件不再重述以避免內容重複。

---

## Phase 6：開發報告

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
| Phase 2（TDD 循環） | `/ecc:tdd-workflow` |
| 程式碼 review | `/ecc:code-review` |
| 安全性檢查 | `/ecc:security-review` |
| Coverage 不足修補 | `/ecc:test-coverage` |

## 附錄：Git Commit 慣例（單一來源）

各 Phase 文件不再重述 commit 指令，統一以下表為準：

| 階段 | Prefix | 範例 |
|---|---|---|
| Red | `test:` | `test: add red tests for Order invariants` |
| Green | `feat:` | `feat: implement Order domain layer` |
| Refactor | `refactor:` | `refactor: clean up Order domain layer` |
| Application | `feat:` | `feat: implement CreateOrderService` |
| Adapter | `feat:` | `feat: implement Order REST adapter and JPA adapter` |
| Fix | `fix:` | `fix: handle empty cart in checkout` |
