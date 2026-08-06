# FinSight DataHub

> **AI-Powered Financial Data Warehouse & Market Analytics Platform**
>
> Enterprise-grade financial analytics demonstrating Java 21, Spring Boot 3, PostgreSQL, Redis, React, and Gemini AI integration.

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
# Edit .env — add your Gemini API key and strong JWT secret
```

### 2. Start with Docker Compose (Recommended)
```bash
# First build the React frontend
cd frontend
npm install
npm run build
cd ..

# Build the Spring Boot JAR
cd backend
mvn package -DskipTests
cd ..

# Start all services (PostgreSQL + Redis + Spring Boot)
docker-compose up -d

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
| Analyst | `analyst`| `Analyst@123`| Upload + AI |

---

## 📁 Project Structure

```
FinSight-DataHub/
├── backend/                    # Spring Boot 3 application
│   ├── src/main/java/com/finsight/datahub/
│   │   ├── config/             # SecurityConfig, RedisConfig, SwaggerConfig, WebConfig
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
│   │   ├── ai/                 # Gemini AI integration
│   │   ├── scheduler/          # Folder watcher + cache refresh
│   │   ├── exception/          # Global exception handling
│   │   └── util/               # Date, file, price utilities
│   ├── src/main/resources/
│   │   ├── application.yml     # Master configuration
│   │   ├── application-dev.yml
│   │   ├── application-prod.yml
│   │   └── db/migration/       # Flyway SQL migrations (V1–V9)
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
│   ├── incoming/               # Drop CSVs here for auto-processing
│   ├── archive/                # Successfully processed files
│   ├── error/                  # Failed files + validation reports
│   └── samples/                # Sample CSV files
│       └── stocks_sample.csv   # 50 US stocks (AAPL, MSFT, NVDA, ...)
│
├── docker-compose.yml          # PostgreSQL + Redis + Spring Boot
├── .env.example                # Environment variables template
└── README.md
```

---

## 🏗️ Architecture

```
Browser → Spring Boot (port 8080)
              ├── Serves React SPA (GET /**)
              ├── REST API (GET/POST /api/**)
              ├── ETL Pipeline (Scheduled)
              └── AI Service (Gemini)
                      │              │
                 PostgreSQL        Redis
```

**Key Design Decisions:**
- **Single JAR deployment** — React build bundled into `classpath:/static/`
- **Stateless JWT** — No server-side sessions; token contains userId, role
- **Strategy Pattern** — Separate ETL strategy per asset type (Stock, ETF, Crypto...)
- **Repository layer** — Never write SQL in controllers or services; use JPQL/JPA
- **Cache-Aside** — Redis checked before every expensive analytics query

---

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login → JWT token |
| GET | `/api/auth/me` | Authenticated | Current user profile |

### Upload
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/upload` | ANALYST+ | Upload CSV file |
| GET | `/api/upload/history` | Authenticated | Upload history |

### Analytics
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/analytics/top-gainers` | Authenticated | Top gaining stocks |
| GET | `/api/analytics/top-losers` | Authenticated | Top losing stocks |
| GET | `/api/analytics/sector` | Authenticated | Sector performance |
| GET | `/api/analytics/moving-average` | Authenticated | SMA calculations |

### AI
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/ai/query` | ANALYST+ | Natural language → SQL |
| POST | `/api/ai/market-summary` | Authenticated | AI market summary |

Full documentation: `http://localhost:8080/swagger-ui`

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA |
| Database | PostgreSQL 15 (Flyway migrations, window functions, indexes) |
| Cache | Redis 7 (Spring Cache, per-TTL configuration) |
| Auth | JWT (JJWT 0.12.x), BCrypt strength 12 |
| Frontend | React 18, Vite, MUI v5, Recharts, Axios |
| AI | Google Gemini API (NL→SQL, Market Summaries) |
| Reports | Apache POI (Excel), OpenPDF (PDF) |
| Deployment | Docker, Docker Compose, AWS EC2 ready |

---

## 🗓️ Development Roadmap

| Day | Module | Status |
|-----|--------|--------|
| 1 | Foundation + Authentication | ✅ Complete |
| 2 | ETL Pipeline + CSV Upload | 🔜 Next |
| 3 | Analytics APIs + Redis Cache | 🔜 |
| 4 | AI Module (Gemini) | 🔜 |
| 5 | React Dashboard + Reports | 🔜 |
| 6 | Testing + Docker + Polish | 🔜 |

---

## 🎯 Key Engineering choices

**Why Spring Boot over other frameworks?**
Convention over configuration, massive ecosystem, production-ready out of the box (actuator, security, data).

**Why JWT over sessions?**
Stateless — horizontal scaling without sticky sessions. Each token is self-contained with user ID and role.

**Why Flyway?**
Database migrations as code — version-controlled, reproducible, auditable. Critical for team environments and CI/CD.

**Why Redis Cache-Aside?**
Analytics queries on large datasets are expensive. Cache-aside gives us control: check Redis → miss → query DB → write to Redis → TTL evicts automatically.

**Why Strategy Pattern for ETL?**
Stock CSV differs from Crypto CSV in column names and precision. Strategy pattern adds new asset types without modifying existing code (Open/Closed Principle).
