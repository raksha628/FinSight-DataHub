# FinSight DataHub — Final Interview-Readiness Audit & Stabilization Report

This document certifies that **FinSight DataHub** has been audited, simplified, stabilized, and verified as a focused, high-performance financial data ingestion and analytics platform suitable for Software Engineering internship technical interviews.

---

## 🏛️ 1. Final Architecture

The architecture of the application has been simplified to a robust, stateless 3-tier structure:

```text
                    ┌──────────────────────┐
                    │      React UI        │
                    │   React + MUI        │
                    └──────────┬───────────┘
                               │
                         HTTP / REST
                         JWT Bearer Header
                               │
                               ▼
                    ┌──────────────────────┐
                    │    Spring Boot       │
                    │                      │
                    │  Spring Security     │
                    │        │             │
                    │  Controllers         │
                    │        ↓             │
                    │  Services            │
                    │        ↓             │
                    │  Repositories        │
                    │        ↓             │
                    │  PostgreSQL          │
                    └──────────┬───────────┘
                               │
                               ▼
                           PostgreSQL
```

### Ingestion Pipeline Flow:

```text
CSV Ingestion Trigger (User Ingestion Request via React UI)
                   │
                   ▼
       POST /api/upload Multipart
                   │
                   ▼
  Lookup Strategy from EtlStrategyRegistry
                   │
                   ▼
   Line-by-line Parse via Commons CSV
                   │
                   ▼
     Clean and Validate Row Data
                   │
                   ▼
  Calculate Daily Return (relative to prior close)
                   │
                   ▼
   Bulk Save Entities & Audit Logs (PostgreSQL)
```

There is **NO** automatic folder watcher or background scheduler daemon in this architecture.

---

## 🛠️ 2. Final Technology Stack

- **Backend Framework**: Java 21, Spring Boot 3.3.4, Spring Security, Spring Data JPA
- **Database**: PostgreSQL 15, Flyway Migration Engine (V1-V7)
- **Authentication**: Stateless JSON Web Tokens (JJWT 0.12.3), BCrypt (strength 12)
- **Frontend**: React 18, Vite, Material UI (MUI) v5, Recharts, Axios
- **ETL File Ingestion**: Apache Commons CSV
- **Deployment & Infrastructure**: Docker, Docker Compose

---

## 📋 3. Final Supported Asset Types

The ingestion pipeline handles exactly three asset types:
1. **Stocks** (US Equities OHLCV)
2. **ETFs** (Exchange Traded Funds NAV & AUM)
3. **Mutual Funds** (Scheme Net Asset Value)

---

## ❌ 4. Removed Features

The following features have been completely purged from the codebase:
- **Google Gemini AI integration** (AI Copilot, Prompt helper, Chat Narrator, Explain endpoint)
- **Redis caching** (Redis database container, config class, `@Cacheable`/`@CacheEvict` annotations)
- **PDF/Excel report downloads** (POI/OpenPDF library dependencies)
- **Automated Folder Watcher** (`FolderWatcherScheduler.java`, `@Scheduled` triggers, data folder mounts, paths configuration)
- **Crypto Asset Ingestion** (Crypto entity, repository, strategy, table, test, and frontend code)
- **Forex Ingestion** (Forex entity, repository, strategy, table, test, and frontend code)
- **Sector Performance ETL Ingestion** (Sector performance ETL strategy, table, and data parser)

---

## 💾 5. Final Database Tables

The final PostgreSQL database is normalized and optimized using Flyway migrations `V1` to `V7`:

| Table Name | Primary Keys | Foreign Keys | Key Indexes | Purpose |
|---|---|---|---|---|
| `users` | `id` (BIGINT) | None | `idx_users_username` (Unique), `idx_users_email` (Unique) | User accounts, roles, and hashed credentials |
| `companies` | `id` (BIGINT) | None | `idx_companies_symbol` (Unique) | Reference sector and exchange details for US Stocks |
| `upload_history` | `id` (BIGINT) | `uploaded_by` (FK -> `users`) | `idx_upload_history_uploaded_at` (Sorted) | ETL runs audit logs and validation reports |
| `stocks` | `id` (BIGINT) | `company_id` (FK -> `companies`), `upload_id` (FK -> `upload_history`) | `idx_stocks_company_date` (Composite), `idx_stocks_daily_return` (Sorted) | Daily price records (OHLCV) and daily returns |
| `etfs` | `id` (BIGINT) | `upload_id` (FK -> `upload_history`) | `idx_etfs_symbol`, `idx_etfs_trade_date` | Daily ETF price, NAV, AUM, and expense ratios |
| `mutual_funds` | `id` (BIGINT) | `upload_id` (FK -> `upload_history`) | `idx_mutual_funds_symbol`, `idx_mutual_funds_nav_date` | Daily Mutual Fund scheme prices and NAV |

---

## 📡 6. Final REST Endpoints

### Authentication
- `POST /api/auth/register` (Public): Registers a new user profile.
- `POST /api/auth/login` (Public): Authenticates credentials and returns a signed JWT.
- `GET /api/auth/me` (Authenticated): Retrieves details of the active user.

### Upload (ETL Ingestion)
- `POST /api/upload` (Analyst/Admin): Uploads a multipart CSV for stocks, ETFs, or mutual funds.
- `GET /api/upload/history` (Authenticated): Pagination-supported upload audit history.
- `GET /api/upload/{id}` (Authenticated): Detailed validation report mapping of a specific upload run.

### Analytics (Calculated on-the-fly)
- `GET /api/analytics/top-gainers`: Top daily stock percentage returns.
- `GET /api/analytics/top-losers`: Top daily stock percentage losses.
- `GET /api/analytics/volume`: Most active stocks by volume.
- `GET /api/analytics/sector-avg-price`: Average closing price grouped by market sector.
- `GET /api/analytics/moving-average`: Calculates 20-day and 50-day technical Simple Moving Averages (SMA).
- `GET /api/analytics/highest-close`: Peak close price in range.
- `GET /api/analytics/lowest-close`: Minimum close price in range.
- `GET /api/analytics/most-active`: Highest trading volume records.

### Report Exports
- `GET /api/reports/export/sector-performance`: Downloads sector performance CSV.
- `GET /api/reports/export/gainers-losers`: Downloads gainers/losers CSV.
- `GET /api/reports/export/moving-averages`: Downloads technical averages CSV.
- `GET /api/reports/export/etl-audit`: Downloads ETL audit logs CSV.

---

## ☕ 7. Java 21 Verification

- **Configured Java**: **Java 21** (`<java.version>21</java.version>` in `pom.xml`, and baseline image `eclipse-temurin:21-jre-alpine` in `Dockerfile`).
- **Java actually used for Maven**: **Java 24** (runtime active version on local environment).
- **Maven version**: **Apache Maven 3.9.16**

`JDK 21 verification could not be completed because JDK 21 is unavailable in the current environment.`

The build was successfully compiled and tested using **JDK 24** as the compiler runtime environment, targeting Java 21 dependencies.

---

## 🧪 8. Test Results

The test suite runs with 0 compilation warnings and finishes with a successful exit code:

- **Tests Executed**: **25**
- **Failures**: **0**
- **Errors**: **0**
- **Skipped**: **0**
- **Build Result**: **`BUILD SUCCESS`**

---

## 💾 9. Fresh Database Verification

Starting from an empty PostgreSQL instance, Flyway successfully runs migrations `V1` to `V7` sequentially:
- `V1`: Creates `users` table.
- `V2`: Creates `companies` table.
- `V3`: Creates `upload_history` table.
- `V4`: Creates `stocks` table.
- `V5`: Creates `etfs` table.
- `V6`: Creates `mutual_funds` table.
- `V7`: Seeds default user accounts (`admin`/`analyst`) and 50 US companies.

---

## 🐳 10. Docker Verification

Docker Compose starts two services:
1. `postgres`: Exposes port 5432 and mounts a named data volume (`finsight_postgres_data`) for persistence.
2. `finsight-app`: Builds the backend jar containing static React assets and runs on port 8080.
Isolating network is achieved via a dedicated bridge driver network (`finsight-network`).

---

## 🔍 11. End-to-End Verification

The following flows are fully verified:
1. **Security**: Public endpoints are open; accessing protected analytics endpoints without carrying a valid JWT header returns `401 Unauthorized`.
2. **Access Lifecycle**: Admin/Analyst login generates a signed HMAC-SHA256 JWT containing subject claims and expiration metadata.
3. **Ingestion Strategy**: Uploading a CSV file triggers Strategy pattern detection, parses rows using Apache Commons CSV, validates positive metrics, links stocks to target companies, computes daily returns, and records audit history.
4. **Calculated Analytics**: Sector averages, moving averages (SMA-20/50), and top daily gainers are calculated on-the-fly via set-based queries in PostgreSQL.

---

## 💡 12. Remaining Complexity

- **Strategy Pattern Registry**: Autowiring concrete strategy beans (`StockEtlStrategy`, `EtfEtlStrategy`, `MutualFundEtlStrategy`) directly into a dynamic Map decouples controller from file parsing details. Be prepared to explain generic map autowiring.
- **SQL Analytics Optimization**: Aggregations are performed using database queries rather than JVM processing. If asked why, discuss heap memory limits, DB index scans vs. table scans, and how database index structures (e.g., composite indexes) are utilized.

---

## 🎯 13. Interview Topics I Must Master

- **Java**: OOP Principles, Strategy Design Pattern, Collections (`EnumMap`, `List`), BigDecimal (precision for price metrics), and Exception Handling.
- **Spring Boot**: Dependency Injection, REST APIs, DTO pattern, Global Exception Handling (`@RestControllerAdvice`), configurations.
- **Spring Security**: Filter chain interception sequence, Stateless access control, JWT cryptographic signing, and BCrypt password hashing.
- **Database**: Normalization, Foreign key constraints, Composite Indexes, Query optimization, and database window/aggregation functions.
- **React**: State hooks (`useState`, `useEffect`), client routing, Axios requests, and component UI grids.
- **Docker**: Container configuration, persistent volumes, bridge networks, and environment variables.

---

## 🎓 14. Final Verdict

**INTERVIEW READY**
