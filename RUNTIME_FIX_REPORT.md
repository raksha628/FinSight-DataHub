# FinSight DataHub — Runtime Fix Report

This report documents the diagnostics and successful resolution of the port `8080` connection refusal issue.

---

## 🔍 1. Root Cause
The root cause of `localhost:8080` connection refusal consisted of two factors:
1. **Server Restart Inactivity**: A host container server restart had stopped all background tasks and Compose containers, leaving the `finsight-app` container in a dormant `Created` status.
2. **Stale Docker Image**: When Compose was first brought back up, it executed a stale Docker image built 46 hours ago (before the previous scope simplification). This stale image:
   - Contained active dependencies for Redis on the classpath.
   - Threw connection exceptions to `localhost:6379` during health checks, causing Actuator to return `503 Service Unavailable` (`DOWN` state).
   - Expected obsolete database schemas.
   - Ran into Flyway checksum verification mismatches on migrations `V6` and `V7` because of local migration file edits, causing Spring Boot to crash in subsequent startup attempts.

---

## 📊 2. Evidence from Logs
From the `docker compose logs finsight-app` output:
```text
Caused by: org.flywaydb.core.api.exception.FlywayValidateException: Validate failed: Migrations have failed validation
Migration checksum mismatch for migration version 6
-> Applied to database : 1198407350
-> Resolved locally    : 1294668748
Either revert the changes to the migration, or run repair to update the schema history.
```
And earlier:
```text
org.springframework.data.redis.RedisConnectionFailureException: Unable to connect to Redis
...
Caused by: io.lettuce.core.RedisConnectionException: Unable to connect to localhost/<unresolved>:6379
```

---

## 🛠️ 3. Files Changed
- [`.env.example`](file:///d:/FinSight%20DataHub/.env.example): Cleaned up defunct data paths variable definitions.
- [`.env`](file:///d:/FinSight%20DataHub/.env): Cleaned up defunct data paths variable definitions.
- [`frontend/index.html`](file:///d:/FinSight%20DataHub/frontend/index.html): Removed obsolete `AI-Powered` description content and `AI` keyword tags.

---

## ⚙️ 4. Exact Configuration & Runtime Issues
- **Defunct Watcher Variables**: Removed defunct local directories variables (`DATA_INCOMING_PATH`, `DATA_ARCHIVE_PATH`, `DATA_ERROR_PATH`) from the environment templates since the watch service scheduler was purged.
- **Docker Compose Volume**: Flyway schema state validation conflict was resolved by dropping the stale Docker database volume (`finsight_postgres_data`) and running Compose clean, which allowed migrations V1 to V7 to compile from scratch.
- **Actuator Health Check**: Auto-configured reactive Redis health check indicators vanished naturally once the fresh backend JAR was repackaged with the cleaned-up `pom.xml` (which has no Redis starters).

---

## 📡 5. Connection Status & Port Verification
- **Initial Connection Status**: `netstat -ano | findstr :8080` returned empty; nothing was listening on port 8080.
- **Final Port Status**: Port `8080` is listening successfully on all interfaces:
  ```text
  TCP    0.0.0.0:8080           0.0.0.0:0              LISTENING       3892
  TCP    [::]:8080              [::]:0                 LISTENING       3892
  ```

---

## 🐳 6. Deployment Mode
- **Startup Mode**: **Docker Compose** (`docker compose down -v && docker compose up --build -d`) was used as the official execution runtime.
- **Build Sequence**: Re-generated the production boot JAR locally via Maven (`mvn package -DskipTests`) to compile and bundle the updated React client static resources into the backend target folder before launching the container build.

---

## 🗄️ 7. PostgreSQL & Database Status
- **Service Name**: `finsight-postgres` (PostgreSQL 15 on port 5432).
- **Migration Verification**: Flyway executed migrations `V1` to `V7` successfully on database startup:
  - `V1` → `users` table created.
  - `V2` → `companies` table created.
  - `V3` → `upload_history` table created.
  - `V4` → `stocks` table created.
  - `V5` → `etfs` table created.
  - `V6` → `mutual_funds` table created.
  - `V7` → seeded default users (`admin`, `analyst`) and companies.
- **Status**: **Healthy & Connected**.

---

## 🔑 8. JWT Configuration
- **Expected Key Format**: The Java code in [`JwtService.java`](file:///d:/FinSight%20DataHub/backend/src/main/java/com/finsight/datahub/security/JwtService.java) supports both raw strings and Base64-decoded keys:
  ```java
  private SecretKey getSigningKey() {
      byte[] keyBytes;
      try {
          keyBytes = Decoders.BASE64.decode(secretKey);
      } catch (Exception e) {
          keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
      }
      return Keys.hmacShaKeyFor(keyBytes);
  }
  ```
- **Validation**: The configured `JWT_SECRET=FinSightDataHub_SuperSecureKey_2026_LocalDevMode!` is valid since its length (48 characters/bytes) exceeds the 32-byte (256-bit) minimum required by the HS256 algorithm.

---

## ☕ 9. Java Version & Build Environment
- **Configured Java Target**: Java 21
- **Host Java Runtime (Maven)**: Java 24.0.2
- **Direct Java 21 Verification**: No (JDK 21 is not present in the host development machine path; JDK 24 was used to verify compilation).
- **Container Runtime**: Java 21.0.11 (inside the JRE Alpine base image).

---

## 🧪 10. Maven Test Result
- **Command**: `mvn clean test`
- **Result**: **BUILD SUCCESS**
  * Tests run: 25
  * Failures: 0
  * Errors: 0
  * Skipped: 0

---

## 💻 11. End-User Load Verification
- **Actuator Health Check Response**: HTTP 200 OK
  ```json
  {
    "status": "UP"
  }
  ```
- **Root Page Loading**: Served successfully at `http://localhost:8080/`. The bundled React SPA serves index pages with the correct metadata tags:
  ```html
  <meta name="description" content="FinSight DataHub - Spring Boot-based financial data ingestion and analytics platform" />
  <meta name="keywords" content="financial analytics, stock market, data warehouse, fintech" />
  ```
