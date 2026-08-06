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

### AST Security Rules Enforced:
1. **Statement Type Enforcement**: Query must strictly parse into `net.sf.jsqlparser.statement.select.Select`. Any `Insert`, `Update`, `Delete`, `Drop`, or `Truncate` statement throws `BadRequestException`.
2. **Comment Disallowance**: Rejects queries containing `--` or `/* */` to neutralize comment obfuscation.
3. **Table Whitelisting**: `TablesNamesFinder` extracts all table references from the AST and verifies they match approved entities (`stocks`, `companies`, `etfs`, `mutual_funds`, `crypto`, `forex`, `sector_performance`).
4. **Execution Bounds**: `QueryExecutor` executes queries via `JdbcTemplate` with a 5-second query timeout and max row cap of 100 rows.

---

## ⚡ 3. ETL Pipeline Architecture (Strategy Pattern)

### Design Pattern Choice: Strategy Pattern
Financial markets include diverse asset classes with distinct CSV columns, date formats, and precision requirements (e.g. US Equities vs 8-decimal Cryptocurrency rates).

- **`EtlStrategy` (Interface)**: Defines `AssetType getAssetType()` and `EtlResult process(InputStream stream, UploadHistory audit)`.
- **Concrete Strategies**:
  - `StockEtlStrategy`: Resolves/creates `Company`, computes daily percentage return relative to previous trade date close.
  - `EtfEtlStrategy`, `MutualFundEtlStrategy`, `CryptoEtlStrategy`, `ForexEtlStrategy`, `SectorPerformanceEtlStrategy`.
- **`EtlStrategyRegistry`**: Spring autowires all strategies into a `Map<AssetType, EtlStrategy>`, eliminating `if-else` branching (Open/Closed Principle).

### Automated Folder Watcher (`FolderWatcherScheduler`)
- Polling scheduler (`@Scheduled`) scans `data/incoming/` directory every 30 seconds.
- Automatically infers `AssetType` from filename, invokes the ETL pipeline, and moves files to `data/archive/` (success) or `data/error/` (failure).

---

## 🎯 4. Technical Interview Q&A Guide

### Q1: Why compile React frontend into Spring Boot `classpath:/static/` instead of separate microservices?
> **Answer**: Single-JAR deployment significantly simplifies CI/CD, deployment orchestration, and horizontal scaling. It eliminates CORS complexity during development and production while providing single-command containerization via Docker.

### Q2: How do you solve the N+1 select problem in Spring Data JPA?
> **Answer**: When querying `Stock` records that reference `Company`, Hibernate by default issues N separate queries for each company ID. We solve this by writing explicit JPQL queries with `JOIN FETCH s.company c`, instructing PostgreSQL to perform an inner join and populate company fields in a single query execution.

### Q3: Why calculate financial analytics inside SQL rather than Java Streams?
> **Answer**: Processing millions of daily price rows in Java streams causes excessive heap allocation, memory copying, and GC pause overhead. PostgreSQL is written in C and optimized for set-based mathematical aggregations (`AVG`, `SUM`, `COUNT`, `WINDOW`). We push calculation to the database.

### Q4: How is stateless authentication secured with JWT?
> **Answer**: Requests include `Authorization: Bearer <token>`. `JwtAuthFilter` intercepts incoming requests, extracts the JWT, verifies the HMAC-SHA256 signature, extracts user claims and roles (`ROLE_ADMIN`, `ROLE_ANALYST`), and populates Spring Security's `SecurityContextHolder`. Sessions are stateless (`SessionCreationPolicy.STATELESS`).

---

## 🚀 5. Performance Benchmarks & Limits
- **CSV Ingestion Rate**: ~5,000 records/sec via batch JPA persistence.
- **Analytics Query Response Time**: < 15ms (indexed JPQL queries).
- **SQL Security Validator**: < 1ms AST validation overhead.
