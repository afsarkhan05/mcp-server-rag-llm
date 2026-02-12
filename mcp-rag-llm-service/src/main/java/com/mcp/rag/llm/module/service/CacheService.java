package com.mcp.rag.llm.module.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Slf4j
@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${cache.ttl:3600}")
    private long cacheTTL;

    @Value("${cache.enabled:true}")
    private boolean cacheEnabled;

    /**
     * Generate cache key for query
     */
    public String generateCacheKey(String prefix, String query) {
        // Normalize query: lowercase, trim, remove extra spaces
        String normalized = query.toLowerCase().trim().replaceAll("\\s+", " ");
        return String.format("%s:%s", prefix, normalized);
    }

    /**
     * Get cached result
     */
    public <T> T getCachedResult(String key, Class<T> type) {
        if (!cacheEnabled) {
            return null;
        }

        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached == null) {
                return null;
            }

            if (type.isInstance(cached)) {
                return type.cast(cached);
            }

            // If it's a String, try to deserialize
            if (cached instanceof String) {
                return objectMapper.readValue((String) cached, type);
            }

            return null;
        } catch (Exception e) {
            log.warn("Error reading from cache: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Cache result
     */
    public void cacheResult(String key, Object result) {
        if (!cacheEnabled) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(key, result, Duration.ofSeconds(cacheTTL));
            log.debug("Cached result for key: {}", key);
        } catch (Exception e) {
            log.warn("Error writing to cache: {}", e.getMessage());
        }
    }

    /**
     * Invalidate cache by pattern
     */
    public void invalidateCache(String pattern) {
        if (!cacheEnabled) {
            return;
        }

        try {
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Invalidated {} cache entries matching: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.warn("Error invalidating cache: {}", e.getMessage());
        }
    }

    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        try {
            var keys = redisTemplate.keys("*");
            long totalKeys = keys != null ? keys.size() : 0;

            return new CacheStats(
                    cacheEnabled,
                    totalKeys,
                    cacheTTL
            );
        } catch (Exception e) {
            return new CacheStats(cacheEnabled, 0, cacheTTL);
        }
    }

    public record CacheStats(boolean enabled, long totalKeys, long ttl) {}
}
