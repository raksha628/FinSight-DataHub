# ═══════════════════════════════════════════════════════════════════
# FinSight DataHub — Backend Dockerfile
# Lean JRE 21 Runtime Image
# ═══════════════════════════════════════════════════════════════════

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built application JAR
COPY target/datahub-*.jar app.jar

# Create data directories for ETL file pipeline & set non-root ownership
RUN mkdir -p /app/data/incoming /app/data/archive /app/data/error /app/logs \
    && addgroup -S finsight && adduser -S finsight -G finsight \
    && chown -R finsight:finsight /app

USER finsight

# Spring Boot runs on port 8080
EXPOSE 8080

# JVM tuning for containers: respect cgroup memory limits
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
