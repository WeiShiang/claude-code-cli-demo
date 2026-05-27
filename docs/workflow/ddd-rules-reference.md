# DDD Rules 參考表

> 本檔列出 `ddd@context-engineering-kit` plugin 的 14 條規則。
> 主流程文件：`../agent-workflow-ddd-tdd.md`

## 全規則對照

| Rule | Impact | 適用 Phase | 重點 |
|---|---|---|---|
| `clean-architecture-ddd` | HIGH | 1, 4 | Domain 層無 framework import |
| `functional-core-imperative-shell` | HIGH | 2, 3 | 純 domain 邏輯，副作用在外層 |
| `separation-of-concerns` | HIGH | 1, 4 | 單一職責，分層清楚 |
| `domain-specific-naming` | HIGH | 1, 2 | 使用 Ubiquitous Language，禁用泛用詞 |
| `command-query-separation` | HIGH | 1, 2 | 查詢無副作用，命令無回傳 |
| `explicit-side-effects` | HIGH | 1, 3 | 副作用明確標示在方法/呼叫處 |
| `explicit-data-flow` | HIGH | 1, 2 | 透過回傳值傳遞結果，不靠 mutation |
| `error-handling` | HIGH | 2, 3 | Typed catch + 帶 context log + rethrow |
| `principle-of-least-astonishment` | HIGH | 1, 2, 3 | 方法只做名稱承諾的事 |
| `library-first-approach` | HIGH | 4 | 優先用成熟函式庫，避免重造輪子 |
| `early-return-pattern` | MEDIUM | 2 | 錯誤路徑先返回，主路徑無巢狀 |
| `explicit-control-flow` | MEDIUM | 2 | 控制流程明確可追蹤 |
| `function-file-size-limits` | MEDIUM | 2 | 方法 ≤ 20 行、class ≤ 200 行 |
| `call-site-honesty` | MEDIUM | 3 | Logging 在呼叫處可見，不藏在 helper |

## 各 Phase 觸發規則

| Phase | 主要規則 |
|---|---|
| Phase 1 設計 | `clean-architecture-ddd`、`separation-of-concerns`、`domain-specific-naming`、`command-query-separation`、`explicit-side-effects`、`principle-of-least-astonishment`、`explicit-data-flow` |
| Phase 2 TDD 循環（Green） | `functional-core-imperative-shell`、`explicit-data-flow`、`error-handling` |
| Phase 2 TDD 循環（Refactor） | `domain-specific-naming`、`function-file-size-limits`、`early-return-pattern`、`explicit-control-flow`、`command-query-separation`、`principle-of-least-astonishment`、`explicit-data-flow`、`error-handling` |
| Phase 3 Application | `functional-core-imperative-shell`、`explicit-side-effects`、`error-handling`、`call-site-honesty`、`principle-of-least-astonishment` |
| Phase 4 Adapter | `library-first-approach`、`clean-architecture-ddd`、`separation-of-concerns` |
