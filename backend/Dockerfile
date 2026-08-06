# ═══════════════════════════════════════════════════════════════════
# FinSight DataHub — Backend Dockerfile
# Multi-stage build: Build stage + Lean runtime stage
# ═══════════════════════════════════════════════════════════════════

# ── Stage 1: Build ─────────────────────────────────────────────────
# Uses Maven with JDK 21 to build the JAR.
# The frontend is pre-built and copied into src/main/resources/static
# before this Dockerfile runs (via docker-compose build context).
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy pom.xml first and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B -Dskip.npm=true

# Copy source and build (skip npm — React is pre-built)
COPY src ./src
RUN mvn package -DskipTests -Dskip.npm=true

# ── Stage 2: Runtime ───────────────────────────────────────────────
# Minimal JRE 21 image — no Maven, no source code
FROM eclipse-temurin:21-jre-alpine

# Security: run as non-root user
RUN addgroup -S finsight && adduser -S finsight -G finsight
USER finsight

WORKDIR /app

# Copy only the built JAR
COPY --from=build /app/target/datahub-*.jar app.jar

# Create data directories for ETL file pipeline
RUN mkdir -p /app/data/incoming /app/data/archive /app/data/error /app/logs

# Spring Boot runs on port 8080
EXPOSE 8080

# JVM tuning for containers: respect cgroup memory limits
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
