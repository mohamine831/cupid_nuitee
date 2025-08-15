package com.example.cupid.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final CacheManager cacheManager;

    public <T> T getFromCache(String cacheName, String key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                log.debug("Cache hit for cache: {}, key: {}", cacheName, key);
                return type.cast(wrapper.get());
            }
        }
        log.debug("Cache miss for cache: {}, key: {}", cacheName, key);
        return null;
    }

    public void putInCache(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
            log.debug("Cached value for cache: {}, key: {}", cacheName, key);
        }
    }

    public void evictFromCache(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.debug("Evicted cache for cache: {}, key: {}", cacheName, key);
        }
    }

    public void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.debug("Cleared cache: {}", cacheName);
        }
    }

    public <T> T getOrLoad(String cacheName, String key, Callable<T> loader, Class<T> type) {
        T cachedValue = getFromCache(cacheName, key, type);
        if (cachedValue != null) {
            return cachedValue;
        }

        try {
            T loadedValue = loader.call();
            if (loadedValue != null) {
                putInCache(cacheName, key, loadedValue);
            }
            return loadedValue;
        } catch (Exception e) {
            log.error("Error loading value for cache: {}, key: {}", cacheName, key, e);
            return null;
        }
    }

    public void evictRelatedCaches(String... cacheNames) {
        for (String cacheName : cacheNames) {
            clearCache(cacheName);
        }
        log.debug("Evicted related caches: {}", String.join(", ", cacheNames));
    }
}

