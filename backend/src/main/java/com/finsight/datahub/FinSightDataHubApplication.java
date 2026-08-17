package com.finsight.datahub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FinSight DataHub — Application Entry Point
 *
 * <p>Financial Data Ingestion and Analytics Platform.
 * This application serves both the Spring Boot REST API and the React
 * frontend as a single deployable JAR.</p>
 *
 * <p><b>Architecture:</b>
 * <ul>
 *   <li>REST APIs at {@code /api/**}</li>
 *   <li>React SPA served from {@code /} (static resources in classpath:/static/)</li>
 *   <li>PostgreSQL for persistent storage</li>
 * </ul>
 * </p>
 *
 * @version 1.0.0
 */
@SpringBootApplication
public class FinSightDataHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinSightDataHubApplication.class, args);
    }
}
