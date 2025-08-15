package com.example.cupid.controller;

import com.example.cupid.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheController {

    private final CacheManager cacheManager;
    private final CacheService cacheService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("name", cache.getName());
                cacheInfo.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
                status.put(cacheName, cacheInfo);
            }
        }
        
        return ResponseEntity.ok(status);
    }

    @PostMapping("/clear/{cacheName}")
    public ResponseEntity<String> clearCache(@PathVariable String cacheName) {
        log.info("Clearing cache: {}", cacheName);
        try {
            cacheService.clearCache(cacheName);
            return ResponseEntity.ok("Cache '" + cacheName + "' cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing cache: {}", cacheName, e);
            return ResponseEntity.internalServerError().body("Error clearing cache: " + e.getMessage());
        }
    }

    @PostMapping("/clear/all")
    public ResponseEntity<String> clearAllCaches() {
        log.info("Clearing all caches");
        try {
            cacheService.evictRelatedCaches("properties", "hotels", "reviews", "translations");
            return ResponseEntity.ok("All caches cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing all caches", e);
            return ResponseEntity.internalServerError().body("Error clearing caches: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get cache names once and reuse
        var cacheNames = cacheManager.getCacheNames();
        
        // Add cache statistics if available
        stats.put("totalCaches", cacheNames.size());
        stats.put("cacheNames", cacheNames);
        
        return ResponseEntity.ok(stats);
    }
}

