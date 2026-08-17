# FinSight DataHub

A Spring Boot-based financial data ingestion and analytics platform with JWT authentication, PostgreSQL persistence, and a React dashboard.

## Features

- **Enterprise Spring Boot Architecture** — Thin controller layer, robust service layer, and type-safe data access with Spring Data JPA.
- **Secure JWT Authentication** — Stateless access control with Spring Security and JSON Web Tokens.
- **CSV ETL Ingestion Pipeline** — Streams and ingests financial records directly via a multipart POST request.
- **Strategy Pattern** — Open-Closed Principle applied to parse, validate, and clean different financial asset types (Stocks, ETFs, Mutual Funds).
- **Relational Analytics Database** — PostgreSQL with foreign keys, constraints, and custom indexes designed to optimize query latency.
- **Financial Analytics Engine** — On-the-fly calculations for top gainers, top losers, moving averages (SMA-20, SMA-50), volume analysis, and sector performance.
- **React Bloomberg-style Dashboard** — Rich, dark-theme UI using Material UI (MUI) and Recharts for data visualization.
- **Dockerized Environment** — Orchestrated deployment via Docker Compose.

---

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Node.js 20+
- Docker & Docker Compose
- Maven 3.9+

### 1. Clone and Configure
```bash
git clone https://github.com/yourusername/finsight-datahub.git
cd finsight-datahub
cp .env.example .env
# Edit .env — add your strong JWT secret
```

### 2. Start with Docker Compose (Recommended)
```bash
# First build the React frontend
cd frontend
npm install
npm run build
cd ..

# Start all services (PostgreSQL + Spring Boot backend)
docker-compose up -d --build

# Application runs at: http://localhost:8080
# Swagger UI:          http://localhost:8080/swagger-ui
```

### 3. Development Mode (Hot Reload)
```bash
# Terminal 1 — Spring Boot backend
cd backend
mvn spring-boot:run -Pdev -Dskip.npm=true

# Terminal 2 — React frontend (Vite dev server with proxy)
cd frontend
npm install
npm run dev
# Frontend: http://localhost:3000 (proxied to backend at 8080)
```

---

## 🔐 Default Credentials

| Role    | Username | Password    | Access Level |
|---------|----------|-------------|--------------|
| Admin   | `admin`  | `Admin@123` | Full access  |
| Analyst | `analyst`| `Analyst@123`| Upload CSVs  |

---

## 📁 Project Structure

```
FinSight-DataHub/
├── backend/                    # Spring Boot 3 application
│   ├── src/main/java/com/finsight/datahub/
│   │   ├── config/             # SecurityConfig, SwaggerConfig, WebConfig
│   │   ├── controller/         # REST controllers (thin layer)
│   │   ├── service/            # Business logic interfaces
│   │   │   └── impl/           # Service implementations
│   │   ├── repository/         # Spring Data JPA repositories
│   │   ├── entity/             # JPA entities (DB tables)
│   │   ├── dto/                # Request/Response DTOs
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── mapper/             # MapStruct mappers
│   │   ├── security/           # JWT filter, UserDetails, auth entry point
│   │   ├── etl/                # CSV ETL pipeline
│   │   │   └── strategy/       # Per-asset ETL strategies
│   │   ├── exception/          # Global exception handling
│   │   └── util/               # Date, file, price utilities
│   ├── src/main/resources/
│   │   ├── application.yml     # Master configuration
│   │   ├── application-dev.yml
│   │   ├── application-prod.yml
│   │   └── db/migration/       # Flyway SQL migrations (V1–V7)
│   └── Dockerfile
│
├── frontend/                   # React + Vite dashboard
│   └── src/
│       ├── components/         # Reusable UI components
│       ├── pages/              # Page-level components
│       ├── services/           # Axios API calls
│       ├── context/            # Auth context
│       └── theme/              # MUI dark theme
│
├── data/
│   └── samples/                # Sample CSV files
│       ├── stocks_sample.csv   
│       ├── etf_sample.csv
│       └── mutual_funds_sample.csv
│
├── docker-compose.yml          # PostgreSQL + Spring Boot
├── .env.example                # Environment variables template
└── README.md
```

---

## 🏗️ Architecture

```
Browser → Spring Boot (port 8080)
              ├── Serves React SPA (GET /**)
              ├── REST API (GET/POST /api/**)
              └── ETL Pipeline (Manual upload)
                       │
                  PostgreSQL
```

**Key Design Decisions:**
- **Single JAR deployment** — React build bundled into `classpath:/static/` during compilation.
- **Stateless JWT** — No server-side sessions; the JWT contains the username and role, validated at the filter layer.
- **Strategy Pattern** — Separate ETL strategy per asset type (Stock, ETF, Mutual Fund), adhering to the Open/Closed Principle.
- **Repository layer** — Clean separation of concerns using Spring Data JPA.
- **Database constraints** — Leverages PostgreSQL indexes, unique constraints, and foreign keys to prevent corrupt or duplicate pricing records.

---

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login → JWT token |
| GET | `/api/auth/me` | Authenticated | Current user profile |

### Upload (ETL)
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/upload` | ANALYST+ | Upload CSV file |
| GET | `/api/upload/history` | Authenticated | Ingestion run audit history |

### Analytics
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/analytics/top-gainers` | Authenticated | Daily top gaining stocks |
| GET | `/api/analytics/top-losers` | Authenticated | Daily top losing stocks |
| GET | `/api/analytics/sector` | Authenticated | Sector average close prices |
| GET | `/api/analytics/moving-average` | Authenticated | SMA calculations (SMA-20, SMA-50) |

### Report Exports
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/reports/export/sector-performance` | Authenticated | Export sector performance CSV |
| GET | `/api/reports/export/gainers-losers` | Authenticated | Export gainers/losers CSV |
| GET | `/api/reports/export/moving-averages` | Authenticated | Export technical averages CSV |
| GET | `/api/reports/export/etl-audit` | Authenticated | Export ETL audit logs CSV |

Full Swagger documentation is available at: `http://localhost:8080/swagger-ui`

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA |
| **Database** | PostgreSQL 15 (Flyway migrations, constraints, indexes) |
| **Auth** | JWT (JJWT 0.12.x), BCrypt strength 12 password hashing |
| **Frontend** | React 18, Vite, MUI v5, Recharts, Axios |
| **ETL File Ingestion** | Apache Commons CSV |
| **Deployment** | Docker, Docker Compose |

---

## 🎯 Key Engineering Choices

**Why Spring Boot over other frameworks?**
Convention over configuration, robust built-in support for security, data modeling, and schedulers, and is the industry standard for enterprise application backends.

**Why JWT over sessions?**
Statelessness. It allows the backend to be horizontally scalable since any node can independently verify the token cryptographically without looking up a session store.

**Why Flyway?**
Database migrations as code. This ensures schema modifications are version-controlled, incremental, and fully reproducible across all local dev, test, and production database instances.

**Why Strategy Pattern for ETL?**
Each financial asset class has distinct CSV columns, data cleaning rules, and target tables. Using a strategy pattern allows registering new ingestion pipelines dynamically, meaning we can add support for a new asset type (e.g. bonds) without modifying the central upload controller.
