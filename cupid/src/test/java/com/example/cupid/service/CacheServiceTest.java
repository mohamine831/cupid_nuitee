package com.example.cupid.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private Cache.ValueWrapper valueWrapper;

    @InjectMocks
    private CacheService cacheService;

    private static final String CACHE_NAME = "testCache";
    private static final String CACHE_KEY = "testKey";
    private static final String CACHE_VALUE = "testValue";

    @BeforeEach
    void setUp() {
        // No global setup needed - each test will set up its own mocks
    }

    @Test
    void getFromCache_CacheHit() {
        // Given
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
        when(cache.get(CACHE_KEY)).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(CACHE_VALUE);

        // When
        String result = cacheService.getFromCache(CACHE_NAME, CACHE_KEY, String.class);

        // Then
        assertThat(result).isEqualTo(CACHE_VALUE);
        verify(cacheManager).getCache(CACHE_NAME);
        verify(cache).get(CACHE_KEY);
    }

    @Test
    void getFromCache_CacheMiss() {
        // Given
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
        when(cache.get(CACHE_KEY)).thenReturn(null);

        // When
        String result = cacheService.getFromCache(CACHE_NAME, CACHE_KEY, String.class);

        // Then
        assertThat(result).isNull();
        verify(cacheManager).getCache(CACHE_NAME);
        verify(cache).get(CACHE_KEY);
    }

    @Test
    void getFromCache_CacheNotFound() {
        // Given
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(null);

        // When
        String result = cacheService.getFromCache(CACHE_NAME, CACHE_KEY, String.class);

        // Then
        assertThat(result).isNull();
        verify(cacheManager).getCache(CACHE_NAME);
        verify(cache, never()).get(anyString());
    }

    @Test
    void putInCache_Success() {
        // Given
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
        // When
        cacheService.putInCache(CACHE_NAME, CACHE_KEY, CACHE_VALUE);

        // Then
        verify(cacheManager).getCache(CACHE_NAME);
        verify(cache).put(CACHE_KEY, CACHE_VALUE);
    }

    @Test
    void putInCache_CacheNotFound() {
        // Given
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(null);

        // When
        cacheService.putInCache(CACHE_NAME, CACHE_KEY, CACHE_VALUE);

        // Then
        verify(cacheManager).getCache(CACHE_NAME);
        verify(cache, never()).put(anyString(), any());
    }

    @Test
    void evictFromCache_Success() {
        // Given
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
        // When
        cacheService.evictFromCache(CACHE_NAME, CACHE_KEY);

        // Then
        verify(cacheManager).getCache(CACHE_NAME);
        verify(cache).evict(CACHE_KEY);
    }

    @Test
    void evictFromCache_CacheNotFound() {
        // Given
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(null);

        // When
        cacheService.evictFromCache(CACHE_NAME, CACHE_KEY);

        // Then
        verify(cacheManager).getCache(CACHE_NAME);
        verify(cache, never()).evict(anyString());
    }

    @Test
    void clearCache_Success() {
        // Given
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
        // When
        cacheService.clearCache(CACHE_NAME);

        // Then
        verify(cacheManager).getCache(CACHE_NAME);
        verify(cache).clear();
    }

    @Test
    void clearCache_CacheNotFound() {
        // Given
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(null);

        // When
        cacheService.clearCache(CACHE_NAME);

        // Then
        verify(cacheManager).getCache(CACHE_NAME);
        verify(cache, never()).clear();
    }

    @Test
    void getOrLoad_CacheHit() {
        // Given
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
        when(cache.get(CACHE_KEY)).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(CACHE_VALUE);

        // When
        String result = cacheService.getOrLoad(CACHE_NAME, CACHE_KEY, () -> "loadedValue", String.class);

        // Then
        assertThat(result).isEqualTo(CACHE_VALUE);
        verify(cacheManager).getCache(CACHE_NAME);
        verify(cache).get(CACHE_KEY);
        verify(cache, never()).put(anyString(), any());
    }

    @Test
    void getOrLoad_CacheMiss_Success() {
        // Given
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
        when(cache.get(CACHE_KEY)).thenReturn(null);

        // When
        String result = cacheService.getOrLoad(CACHE_NAME, CACHE_KEY, () -> CACHE_VALUE, String.class);

        // Then
        assertThat(result).isEqualTo(CACHE_VALUE);
        verify(cacheManager, times(2)).getCache(CACHE_NAME); // Called twice: once in getFromCache, once in putInCache
        verify(cache).get(CACHE_KEY);
        verify(cache).put(CACHE_KEY, CACHE_VALUE);
    }

    @Test
    void getOrLoad_CacheMiss_LoaderReturnsNull() {
        // Given
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
        when(cache.get(CACHE_KEY)).thenReturn(null);

        // When
        String result = cacheService.getOrLoad(CACHE_NAME, CACHE_KEY, () -> null, String.class);

        // Then
        assertThat(result).isNull();
        verify(cacheManager).getCache(CACHE_NAME);
        verify(cache).get(CACHE_KEY);
        verify(cache, never()).put(anyString(), any());
    }

    @Test
    void getOrLoad_CacheMiss_LoaderThrowsException() {
        // Given
        when(cacheManager.getCache(CACHE_NAME)).thenReturn(cache);
        when(cache.get(CACHE_KEY)).thenReturn(null);

        // When
        String result = cacheService.getOrLoad(CACHE_NAME, CACHE_KEY, () -> {
            throw new RuntimeException("Loader error");
        }, String.class);

        // Then
        assertThat(result).isNull();
        verify(cacheManager).getCache(CACHE_NAME);
        verify(cache).get(CACHE_KEY);
        verify(cache, never()).put(anyString(), any());
    }

    @Test
    void evictRelatedCaches_Success() {
        // Given
        String[] cacheNames = {"cache1", "cache2", "cache3"};
        when(cacheManager.getCache("cache1")).thenReturn(cache);
        when(cacheManager.getCache("cache2")).thenReturn(cache);
        when(cacheManager.getCache("cache3")).thenReturn(cache);

        // When
        cacheService.evictRelatedCaches(cacheNames);

        // Then
        verify(cacheManager).getCache("cache1");
        verify(cacheManager).getCache("cache2");
        verify(cacheManager).getCache("cache3");
        verify(cache, times(3)).clear();
    }

    @Test
    void evictRelatedCaches_SomeCachesNotFound() {
        // Given
        String[] cacheNames = {"cache1", "cache2", "cache3"};
        when(cacheManager.getCache("cache1")).thenReturn(cache);
        when(cacheManager.getCache("cache2")).thenReturn(null);
        when(cacheManager.getCache("cache3")).thenReturn(cache);

        // When
        cacheService.evictRelatedCaches(cacheNames);

        // Then
        verify(cacheManager).getCache("cache1");
        verify(cacheManager).getCache("cache2");
        verify(cacheManager).getCache("cache3");
        verify(cache, times(2)).clear(); // Only cache1 and cache3 exist
    }
}
