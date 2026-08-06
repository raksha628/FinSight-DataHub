# FinSight DataHub

> **AI-Powered Financial Data Warehouse & Market Analytics Platform**
>
> Enterprise-grade financial analytics system built with Java 21, Spring Boot 3, PostgreSQL, Redis, React, and Google Gemini AI.

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg)](https://www.docker.com/)

---

## 🚀 Key Features

- **Multi-Asset Strategy ETL Pipeline**: Automated CSV ingestion pipeline supporting Stocks, ETFs, Mutual Funds, Cryptocurrencies, Forex, and Sector Performance using the **Strategy Pattern**.
- **Automated Folder Watcher**: Background directory scheduler scanning `data/incoming/` and moving processed files to `data/archive/` or `data/error/`.
- **Set-Based Financial Analytics Engine**: High-performance SQL queries for Top Gainers/Losers, Moving Averages (SMA-20/SMA-50), Sector Distributions, Volume Rankings, and Period Returns.
- **Natural Language → SQL (NL2SQL) Security Engine**: Translates natural language questions to SQL with AST-level security validation using **JSQLParser** (enforcing read-only `SELECT` execution and table whitelisting).
- **Executive Market Brief & Chart Explanation Engine**: Generates Market Health Scores (0-100), overall sentiment, AI executive insights, and narrative explanations for financial charts.
- **Bloomberg-Inspired React Dashboard**: Modern Dark Mode UI built with React 18, Vite, Material UI (MUI v5), Recharts, and Axios.
- **Stateless JWT Security**: BCrypt password hashing, role-based authorization (`ADMIN`, `ANALYST`, `VIEWER`), and OpenAPI 3.0 Swagger documentation.

---

## 🏗️ System Architecture

```
                               ┌──────────────────────────────────────────┐
                               │           React 18 SPA Frontend          │
                               └────────────────────┬─────────────────────┘
                                                    │ REST API / JWT
                                                    ▼
┌────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                          Spring Boot 3 Backend                                         │
│                                                                                                        │
│   ┌─────────────────────┐   ┌───────────────────────────┐   ┌──────────────────────────────────────┐   │
│   │   Security Chain    │   │    Strategy ETL Engine    │   │       Financial Analytics Engine     │   │
│   │ (JWT / Spring Sec)  │   │  (Apache Commons CSV)     │   │ (JPQL Aggregations & Window Func)    │   │
│   └─────────────────────┘   └───────────────────────────┘   └──────────────────────────────────────┘   │
│                                                                                                        │
│   ┌────────────────────────────────────────────────────────────────────────────────────────────────┐   │
│   │                                AI Market Intelligence Engine                                   │   │
│   │  PromptBuilder  │  AST SqlValidator (JSQLParser)  │  QueryExecutor  │  Google Gemini API       │   │
│   └────────────────────────────────────────────────────────────────────────────────────────────────┘   │
└───────────────────────────────────────────────────┬────────────────────────────────────────────────────┘
                                                    │
                                                    ▼
                               ┌──────────────────────────────────────────┐
                               │        PostgreSQL 15 & Redis 7 Cache     │
                               └──────────────────────────────────────────┘
```

---

## 🛠️ Technology Stack

| Layer | Technology | Description |
|---|---|---|
| **Backend** | Java 21, Spring Boot 3.3.4, Spring Security | Core enterprise application framework |
| **Database** | PostgreSQL 15, Flyway 10 | Relational database & version-controlled migrations |
| **Cache** | Redis 7, Spring Cache | Cache-aside for expensive analytics queries |
| **Parsing & Security**| JSQLParser 4.7, Apache Commons CSV | AST-based SQL security validation & CSV ETL |
| **Frontend** | React 18, Vite, MUI v5, Recharts | Financial terminal user interface |
| **AI Integration** | Google Gemini API | Natural Language to SQL & Executive Market Briefs |
| **Containerization** | Docker, Docker Compose | Multi-container environment orchestration |

---

## 🔐 Default Demo Credentials

| Role | Username | Password | Access Level |
|---|---|---|---|
| **Admin** | `admin` | `Admin@123` | Full access (ETL Upload, AI Queries, Admin APIs) |
| **Analyst** | `analyst` | `Analyst@123` | Upload CSVs, AI Queries, Analytics APIs |
| **Viewer** | `viewer` | `Viewer@123` | Read-only Analytics & Dashboard access |

---

## ⚡ Quickstart & Deployment

### 1. Run with Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/yourusername/finsight-datahub.git
cd finsight-datahub

# Start all services (PostgreSQL + Redis + Spring Boot Application)
docker-compose up --build -d

# Application running at: http://localhost:8080
# Swagger OpenAPI UI:     http://localhost:8080/swagger-ui.html
```

### 2. Local Development Mode

```bash
# Terminal 1 — Spring Boot Backend
cd backend
$env:JAVA_HOME="C:\Program Files\Java\jdk-24"; mvn spring-boot:run -Pdev -Dskip.npm=true

# Terminal 2 — React Frontend Dev Server
cd frontend
npm install
npm run dev
# Frontend runs at: http://localhost:3000 (proxied to backend 8080)
```

---

## 📡 API Reference Overview

- **Authentication**: `POST /api/auth/login`, `POST /api/auth/register`, `GET /api/auth/me`
- **ETL Upload**: `POST /api/upload`, `GET /api/upload/history`, `GET /api/upload/{id}`
- **Analytics**: `GET /api/analytics/top-gainers`, `GET /api/analytics/top-losers`, `GET /api/analytics/volume`, `GET /api/analytics/moving-average`, `GET /api/analytics/sector-avg-price`
- **Executive Dashboard**: `GET /api/dashboard/overview`, `GET /api/analytics/summary`
- **AI Intelligence**: `POST /api/ai/query`, `POST /api/ai/market-summary`, `POST /api/ai/explain`, `GET /api/ai/history`

Full OpenAPI 3.0 documentation available at `http://localhost:8080/swagger-ui.html`.

---

## 📚 Technical Interview Guide
Detailed architectural explanations, request lifecycle diagrams, AST SQL security rules, and mock interview questions are documented in [INTERVIEW_GUIDE.md](file:///d:/FinSight%20DataHub/INTERVIEW_GUIDE.md).
