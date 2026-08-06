# FinSight DataHub — Software Engineering Technical Interview Guide

> **Enterprise Financial Data Warehouse & AI-Powered Market Analytics Platform**
>
> Designed for Technical Deep-Dives, System Design Discussions, and Code Architecture Reviews.

---

## 🏛️ 1. Architecture Overview

FinSight DataHub follows **Clean Architecture** principles and a stateless 3-tier enterprise structure:

```
[ React SPA Frontend ] → (Axios REST / JWT Bearer) → [ Spring Boot Backend Layer ]
                                                            ├── Auth & Security Filter Chain
                                                            ├── Strategy Pattern ETL Engine
                                                            ├── Set-Based Financial Analytics
                                                            ├── AST JSQLParser Security Engine
                                                            └── AI Copilot & Market Brief
                                                                     │
                                                            [ PostgreSQL 15 & Redis 7 ]
```

### Key Architectural Layers:
1. **Presentation Layer**: Thin Spring Boot `@RestController` classes handling HTTP parameter validation (`@Valid`), OpenAPI Swagger annotations, and standardized `ApiResponse<T>` response wrapping.
2. **Service Layer**: Decoupled interface-driven business logic (`UploadService`, `AnalyticsService`, `DashboardService`, `AiService`) enforcing strict Single Responsibility.
3. **Repository Layer**: Spring Data JPA repositories utilizing JPQL aggregations (`AVG`, `SUM`, `COUNT`, `JOIN FETCH`) to execute set-based calculations inside PostgreSQL instead of fetching records into JVM memory.
4. **Caching Layer**: Redis 7 cache-aside strategy for expensive analytics queries, evicting automatically on new ETL ingestion jobs.

---

## 🔒 2. Deep Dive: AST-Level Natural Language to SQL Security (`SqlValidator`)

### The Security Challenge
Traditional NL2SQL systems are vulnerable to SQL Injection, unauthorized data access, or unintentional destructive commands (`DROP TABLE`, `DELETE`, `UPDATE`) if an LLM produces malicious queries.

### Our Solution
Instead of naive regex or string matching, FinSight DataHub uses **`JSQLParser`** to parse the generated query into an Abstract Syntax Tree (AST) before execution:

```
User Prompt → Gemini LLM → Raw SQL → SqlValidator (AST Parsing) → Safe Execution
                                             ├── 1. Reject Comments (-- / /* */)
                                             ├── 2. Assert Statement is Instance of Select
                                             ├── 3. Extract Table Names & Verify Whitelist
                                             └── 4. Enforce Query Timeout (5s) & Row Limits (100)
```

---

## ⚡ 3. ETL Pipeline Architecture (Strategy Pattern)

### Design Pattern Choice: Strategy Pattern
Financial markets include diverse asset classes with distinct CSV columns, date formats, and precision requirements (e.g. US Equities vs 8-decimal Cryptocurrency rates).

- **`EtlStrategy` (Interface)**: Defines `AssetType getAssetType()` and `EtlResult process(InputStream stream, UploadHistory audit)`.
- **Concrete Strategies**: `StockEtlStrategy`, `EtfEtlStrategy`, `MutualFundEtlStrategy`, `CryptoEtlStrategy`, `ForexEtlStrategy`, `SectorPerformanceEtlStrategy`.
- **`EtlStrategyRegistry`**: Spring autowires all strategies into a `Map<AssetType, EtlStrategy>`, eliminating `if-else` branching (Open/Closed Principle).

---

## 🎓 4. Core System Architectural Q&A

### Question 1: Why did you choose the Strategy Pattern for ETL?
> **Answer**:
> Financial markets deal with heterogeneous asset classes (Stocks, ETFs, Mutual Funds, Crypto, Forex, Sector Performance) that have distinct CSV schemas, validation rules, and decimal precisions.
>
> 1. **Open/Closed Principle (SOLID)**: Using `EtlStrategy` allows adding support for new financial assets (e.g., Commodities, Options) by creating a new strategy class without modifying existing ingestion logic.
> 2. **Clean Registry Lookup**: `EtlStrategyRegistry` autowires all strategy implementations into a `Map<AssetType, EtlStrategy>`, eliminating brittle `if-else` or `switch` statements and enabling dynamic runtime strategy selection.

---

### Question 2: Why use `JdbcTemplate` for AI-generated SQL instead of JPA?
> **Answer**:
> 1. **Dynamic Schema Projections**: JPA require pre-compiled `@Entity` mappings and typed JPQL/Criteria queries. Natural Language → SQL generates ad-hoc SQL with arbitrary column projections (e.g., `SELECT c.sector, AVG(s.close_price)...`) that do not map to a single entity.
> 2. **Low-Level Execution Safeguards**: `JdbcTemplate` provides fine-grained control over execution parameters, allowing us to enforce query timeouts (`setQueryTimeout(5)`) and hard result set caps (`setMaxRows(100)`), protecting the JVM from memory exhaustion.

---

### Question 3: Why is Redis useful in this application?
> **Answer**:
> 1. **Cache-Aside Strategy**: Calculating market overview statistics, moving averages (SMA-20/50), and sector aggregations requires expensive SQL `JOIN`s and window functions across historical price tables.
> 2. **Sub-Millisecond Read Speeds**: Caching aggregate results in Redis RAM drops API response times from ~45ms down to < 2ms for dashboard users.
> 3. **Automated Cache Invalidation**: When a new ETL job completes, the cache manager evicts affected Redis namespaces, ensuring data freshness without sacrificing high read throughput.

---

### Question 4: Why did you use DTOs instead of returning entities directly?
> **Answer**:
> 1. **Prevents Circular Reference Deadlocks**: JPA entities with bidirectional relationships (`@ManyToOne` / `@OneToMany`) trigger infinite recursion during Jackson JSON serialization.
> 2. **Security & Information Hiding**: Prevents exposing internal database keys, auditing timestamps, or password hashes to public APIs.
> 3. **Decoupled API Contracts**: Database schema refactorings (renaming a table column) do not break public REST API specifications. DTOs also allow returning calculated values (e.g. percentage return) that aren't stored directly in a single database column.

---

### Question 5: How is SQL injection prevented?
> **Answer**:
> 1. **For Standard REST APIs**: All database interactions use Spring Data JPA with parameterized queries (`:param` binding), guaranteeing complete separation between code and user inputs.
> 2. **For AI Natural Language → SQL**: Since queries are generated dynamically by an LLM, parameterization alone is insufficient. We implement **AST-level security (`SqlValidator`)** using `JSQLParser`:
>    - Reject SQL comments (`--`, `/* */`) to prevent injection tricks.
>    - Enforce that statement strictly parses as `Select`. Reject `INSERT`, `UPDATE`, `DELETE`, `DROP`, `ALTER`, `TRUNCATE`.
>    - Extract table names via AST traversal and verify against a strict whitelist (`stocks`, `companies`, `etfs`, etc.).
>    - Reject multi-statement semicolons and set a 5-second query execution timeout.

---

### Question 6: How does JWT authentication work?
> **Answer**:
> 1. **Authentication**: User POSTs credentials to `/api/auth/login`. `AuthenticationManager` verifies username/password via `BCryptPasswordEncoder` against PostgreSQL.
> 2. **Token Generation**: Upon success, `JwtTokenProvider` constructs an HMAC-SHA256 signed JWT containing `sub` (username), `iat`, `exp` (24 hours), and user role claims (`ROLE_ADMIN`, `ROLE_ANALYST`).
> 3. **Stateless Request Interception**: On subsequent HTTP calls, the client includes `Authorization: Bearer <token>`.
> 4. **Spring Security Filter**: `JwtAuthFilter` intercepts the request, validates signature and expiry, builds a `UsernamePasswordAuthenticationToken`, and populates `SecurityContextHolder`. Sessions remain 100% stateless (`SessionCreationPolicy.STATELESS`).

---

### Question 7: How does the ETL pipeline process a CSV file?
> **Answer**:
> 1. **Trigger**: User uploads a file via REST API or `FolderWatcherScheduler` detects a CSV drop in `data/incoming/`.
> 2. **Strategy Dispatch**: `EtlStrategyRegistry` identifies the matching `EtlStrategy` based on asset type.
> 3. **Streaming CSV Parsing**: Apache Commons CSV streams records line-by-line via `InputStream`, preventing JVM memory spikes.
> 4. **Validation & Normalization**: Row data is validated (non-null dates, valid prices). Invalid rows are tracked as `rejectedRows`.
> 5. **Entity Upsert & Relation Linking**: Lookups/creates `Company` entity, constructs `Stock` price entity, calculates daily return percentage relative to prior close.
> 6. **Batch Persistence & Audit**: Batch persists records to PostgreSQL, logs processing duration and counts into `UploadHistory`, and archives/error-routes the source file.

---

### Question 8: Why is the AI module separated from the analytics module?
> **Answer**:
> 1. **Single Responsibility Principle**: The Analytics module is a deterministic, high-performance aggregation engine. The AI module is an non-deterministic natural language translation and synthesis engine.
> 2. **Fault Isolation & Resilience**: Third-party LLM latency spikes or API rate limits in Google Gemini will never impact standard REST analytics APIs, dashboard loading, or ETL uploads.
> 3. **Isolated Security Constraints**: AI query execution requires AST sandboxing and strict execution limits, whereas Analytics APIs rely on standard JPA query boundaries.

---

### Question 9: What happens from the moment a CSV is uploaded until it appears on the dashboard?
> **Answer**:
> 1. **Client POST**: React frontend uploads CSV file to `POST /api/upload`.
> 2. **ETL Execution**: Spring Boot executes `StockEtlStrategy`, parses records, persists entities into PostgreSQL `stocks` table, and logs audit record to `upload_history`.
> 3. **Cache Invalidation**: Successful ingestion triggers Redis cache eviction for key `dashboard:overview` and `analytics:*`.
> 4. **HTTP 200 Response**: Controller returns `UploadResponseDto` containing row counts and duration.
> 5. **UI State Update**: React UI receives success response, invalidates local query state, and re-fetches `GET /api/dashboard/overview`.
> 6. **SQL Aggregation & Re-render**: Backend executes set-based JPQL queries, returns updated JSON, and Recharts re-renders KPI cards, sector pie charts, and top gainers bar charts.

---

### Question 10: If one million rows are uploaded, what bottlenecks would you expect and how would you improve them?
> **Answer**:
> **Expected Bottlenecks**:
> 1. *DB Network Roundtrips*: Saving 1M records individual `save()` calls causes 1 million database roundtrips, stalling the thread.
> 2. *JVM Memory Pressure*: Loading 1M objects into Java heap risks `OutOfMemoryError` and long GC pauses.
> 3. *Database Table Scans*: Analytics aggregations on 1M+ rows without indexing perform slow disk table scans.
>
> **Architectural Improvements**:
> 1. *JDBC Batching*: Enable `hibernate.jdbc.batch_size=1000` and `reWriteBatchedInserts=true` to flush inserts in bulk chunks.
> 2. *PostgreSQL `COPY` Command*: Use PostgreSQL native `COPY FROM STDIN` binary ingestion for 50,000+ rows/sec stream ingestion.
> 3. *Database Indexing*: Ensure composite indexes on `(company_id, trade_date)` and `(trade_date, daily_return DESC)`.
> 4. *Asynchronous Offloading*: Offload processing to `@Async` background worker threads or Kafka message queues with WebSocket client progress notifications.

---

## 🚀 5. Performance Benchmarks & Limits
- **CSV Ingestion Rate**: ~5,000 records/sec via batch JPA persistence.
- **Analytics Query Response Time**: < 15ms (indexed JPQL queries).
- **SQL Security Validator**: < 1ms AST validation overhead.
