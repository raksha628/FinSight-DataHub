# FinSight DataHub — Software Engineering Technical Interview Guide

> **Financial Data Ingestion & Market Analytics Platform**
>
> Designed for Technical Deep-Dives, System Design Discussions, and Code Architecture Reviews.

---

## 🏛️ 1. Architecture Overview

FinSight DataHub follows **Clean Architecture** principles and a stateless 3-tier enterprise structure:

```
[ React SPA Frontend ] → (Axios REST / JWT Bearer) → [ Spring Boot Backend Layer ]
                                                            ├── Auth & Security Filter Chain
                                                            ├── Strategy Pattern ETL Engine
                                                            └── Set-Based Financial Analytics
                                                                     │
                                                            [ PostgreSQL 15 ]
```

### Key Architectural Layers:
1. **Presentation Layer**: Thin Spring Boot `@RestController` classes handling HTTP parameter validation (`@Valid`), OpenAPI Swagger annotations, and standardized response wrapping.
2. **Service Layer**: Decoupled interface-driven business logic (`UploadService`, `AnalyticsService`, `DashboardService`) enforcing strict Single Responsibility.
3. **Repository Layer**: Spring Data JPA repositories utilizing JPQL aggregations (`AVG`, `SUM`, `COUNT`, `JOIN FETCH`) to execute set-based calculations inside PostgreSQL instead of fetching records into JVM memory.

---

## ⚡ 2. ETL Pipeline Architecture (Strategy Pattern)

### Design Pattern Choice: Strategy Pattern
Financial markets include diverse asset classes with distinct CSV columns, date formats, and validation rules.

- **`EtlStrategy` (Interface)**: Defines `AssetType getAssetType()` and `EtlResult process(InputStream stream, UploadHistory audit)`.
- **Concrete Strategies**: `StockEtlStrategy`, `EtfEtlStrategy`, `MutualFundEtlStrategy`.
- **`EtlStrategyRegistry`**: Spring autowires all strategies into a `Map<AssetType, EtlStrategy>`, eliminating conditional `if-else` branching when selecting parsing strategies at runtime (adhering to the SOLID Open/Closed Principle).

---

## 🎓 3. Core System Architectural Q&A

### Question 1: Why did you choose the Strategy Pattern for ETL?
> **Answer**:
> Financial markets deal with different asset classes (Stocks, ETFs, Mutual Funds) that have distinct CSV schemas, validation rules, and columns.
>
> 1. **Open/Closed Principle (SOLID)**: Using `EtlStrategy` allows adding support for new financial assets (e.g., Options, Bonds) by creating a new strategy class without modifying existing ingestion logic.
> 2. **Clean Registry Lookup**: `EtlStrategyRegistry` autowires all strategy implementations into a `Map<AssetType, EtlStrategy>`, eliminating brittle `if-else` or `switch` statements and enabling dynamic runtime strategy selection.

---

### Question 2: Why did you use DTOs instead of returning JPA entities directly?
> **Answer**:
> 1. **Prevents Circular Reference Deadlocks**: JPA entities with bidirectional relationships (`@ManyToOne` / `@OneToMany`) trigger infinite recursion during Jackson JSON serialization.
> 2. **Security & Information Hiding**: Prevents exposing internal database auto-generated primary keys, user password hashes, or internal audit fields.
> 3. **Decoupled API Contracts**: Database schema refactorings (renaming a table column) do not break public REST API specifications. DTOs also allow returning calculated values (e.g. moving averages) that aren't stored directly in a single database column.

---

### Question 3: How is SQL injection prevented?
> **Answer**:
> All database interactions use Spring Data JPA with parameterized queries (`:param` binding) or Spring Data Query Methods. This guarantees complete separation between the SQL command structure and user-supplied data variables, eliminating the risk of SQL injection.

---

### Question 4: How does JWT authentication work?
> **Answer**:
> 1. **Authentication**: User POSTs credentials to `/api/auth/login`. `AuthenticationManager` verifies username/password via `BCryptPasswordEncoder` against the PostgreSQL database.
> 2. **Token Generation**: Upon success, `JwtTokenProvider` constructs an HMAC-SHA256 signed JWT containing `sub` (username), `iat`, `exp` (24 hours), and user role claims (`ROLE_ADMIN`, `ROLE_ANALYST`).
> 3. **Stateless Request Interception**: On subsequent HTTP calls, the client includes the header `Authorization: Bearer <token>`.
> 4. **Spring Security Filter**: `JwtAuthFilter` intercepts the request, validates the signature and expiry, builds a `UsernamePasswordAuthenticationToken`, and populates `SecurityContextHolder`. Sessions remain 100% stateless (`SessionCreationPolicy.STATELESS`).

---

### Question 5: How does the ETL pipeline process a CSV file?
> **Answer**:
> 1. **Trigger**: User uploads a file via the REST API multipart request.
> 2. **Strategy Dispatch**: `EtlStrategyRegistry` identifies the matching `EtlStrategy` based on the asset type.
> 3. **Streaming CSV Parsing**: Apache Commons CSV streams records line-by-line via `InputStream`, preventing JVM memory spikes.
> 4. **Validation & Normalization**: Row data is validated (non-null dates, valid positive prices). Invalid rows are tracked as `rejectedRows`.
> 5. **Entity Upsert & Relation Linking**: Looks up or creates the `Company` entity, constructs the price entity (e.g., `Stock`), and calculates daily return percentage relative to the prior close price.
> 6. **Batch Persistence & Audit**: Persists records to PostgreSQL, and logs processing duration and counts into `UploadHistory`.

---

### Question 6: What happens from the moment a CSV is uploaded until it appears on the dashboard?
> **Answer**:
> 1. **Client POST**: React frontend uploads a CSV file to `POST /api/upload`.
> 2. **ETL Execution**: Spring Boot executes the matched `EtlStrategy`, parses records, persists entities into the PostgreSQL database, and logs an audit record to `upload_history`.
> 3. **HTTP 201 Response**: Controller returns `UploadResponseDto` containing row counts and duration.
> 4. **UI State Update**: React UI receives the success response, invalidates local query state, and re-fetches `GET /api/dashboard/overview`.
> 5. **SQL Aggregation & Re-render**: Backend executes set-based JPQL queries, returns updated JSON, and Recharts re-renders KPI cards, sector pie charts, and top gainers bar charts on the React dashboard.

---

### Question 7: If one million rows are uploaded, what bottlenecks would you expect and how would you improve them?
> **Answer**:
> **Expected Bottlenecks**:
> 1. *DB Network Roundtrips*: Saving 1M records using individual `save()` calls causes 1 million database roundtrips, stalling the thread.
> 2. *JVM Memory Pressure*: Loading 1M objects into the Java heap risks `OutOfMemoryError` and long GC pauses.
> 3. *Database Table Scans*: Analytics aggregations on 1M+ rows without indexing perform slow disk table scans.
>
> **Architectural Improvements**:
> 1. *JDBC Batching*: Enable `hibernate.jdbc.batch_size=1000` and `reWriteBatchedInserts=true` to flush inserts in bulk chunks.
> 2. *PostgreSQL `COPY` Command*: Use PostgreSQL native `COPY FROM STDIN` binary ingestion for 50,000+ rows/sec stream ingestion.
> 3. *Database Indexing*: Ensure composite indexes on `(company_id, trade_date)` and `(trade_date, daily_return DESC)`.
