package com.finsight.datahub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration for FinSight DataHub.
 *
 * <p><b>Caching Strategy:</b>
 * <ul>
 *   <li>Keys serialized as UTF-8 strings with namespace prefix {@code finsight:}</li>
 *   <li>Values serialized as JSON (Jackson) — human-readable and debuggable via redis-cli</li>
 *   <li>Per-cache TTL configuration — analytics data expires faster than market summaries</li>
 *   <li>Null values are not cached — prevents caching "no data found" scenarios</li>
 * </ul>
 * </p>
 *
 * <p><b>Cache TTL Reference:</b>
 * <ul>
 *   <li>{@code analytics}    — 1 hour  (default)</li>
 *   <li>{@code market}       — 4 hours</li>
 *   <li>{@code stocks}       — 30 minutes</li>
 *   <li>{@code sector}       — 1 hour</li>
 * </ul>
 * </p>
 */
@Configuration
public class RedisConfig {

    /**
     * Configures the Redis Cache Manager with per-cache TTL settings.
     * Used by Spring's {@code @Cacheable}, {@code @CacheEvict}, etc.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Jackson serializer with Java 8 time module support
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .prefixCacheNameWith("finsight:")
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // Per-cache TTL overrides
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put("analytics",
                defaultConfig.entryTtl(Duration.ofHours(1)));

        cacheConfigurations.put("market",
                defaultConfig.entryTtl(Duration.ofHours(4)));

        cacheConfigurations.put("stocks",
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        cacheConfigurations.put("sector",
                defaultConfig.entryTtl(Duration.ofHours(1)));

        cacheConfigurations.put("search",
                defaultConfig.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * RedisTemplate for direct Redis operations (used in AiQueryService
     * for storing NL query cache and in cache invalidation logic).
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.afterPropertiesSet();
        return template;
    }
}
