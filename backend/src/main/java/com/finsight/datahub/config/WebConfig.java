package com.finsight.datahub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Web MVC Configuration — CORS and React SPA Routing Support.
 *
 * <p><b>SPA Routing Problem:</b>
 * React Router uses client-side routing (e.g., {@code /dashboard}, {@code /stocks}).
 * When the user refreshes the browser on these routes, the request goes to Spring Boot.
 * Spring Boot would return 404 since there's no controller for {@code /dashboard}.
 * </p>
 *
 * <p><b>Solution:</b>
 * The custom {@link PathResourceResolver} falls back to {@code /index.html}
 * for any request that doesn't match a real static file. React Router then
 * handles the route client-side.
 * </p>
 *
 * <p><b>CORS:</b>
 * In development, React runs at {@code localhost:3000} while Spring Boot runs
 * at {@code localhost:8080}. CORS is configured to allow cross-origin requests
 * from the Vite dev server.
 * In production, the React build is bundled into Spring Boot and served from
 * the same origin — CORS is not needed, but the config is kept for flexibility.
 * </p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Configures the static resource handler with SPA fallback.
     * Any request for a non-existent static resource will return index.html.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location)
                            throws IOException {
                        Resource resource = location.createRelative(resourcePath);
                        // Return the real static file if it exists
                        if (resource.exists() && resource.isReadable()) {
                            return resource;
                        }
                        // Fallback to index.html for React SPA client-side routing
                        ClassPathResource indexHtml = new ClassPathResource("/static/index.html");
                        return indexHtml.exists() ? indexHtml : null;
                    }
                });
    }

    /**
     * CORS configuration allowing the React Vite dev server during development.
     * In production (single-JAR deploy), same-origin requests don't need CORS.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000",    // Vite dev server
                        "http://localhost:8080"      // Spring Boot (same-origin in prod)
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
