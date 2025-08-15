package com.example.cupid.controller;

import com.example.cupid.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CacheController.class)
class CacheControllerTest {

    @MockBean
    private CacheManager cacheManager;

    @MockBean
    private CacheService cacheService;

    @MockBean
    private Cache cache;

    @Autowired
    private CacheController cacheController;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Reset all mocks before each test to ensure clean state
        reset(cacheManager, cacheService, cache);
        
        // Set up default cache mock behavior
        when(cache.getName()).thenReturn("testCache");
        when(cache.getNativeCache()).thenReturn(new HashMap<>());
    }

    @Test
    void getCacheStatus_Success() throws Exception {
        // Given
        when(cacheManager.getCacheNames()).thenReturn(Arrays.asList("properties", "hotels"));
        when(cacheManager.getCache("properties")).thenReturn(cache);
        when(cacheManager.getCache("hotels")).thenReturn(cache);
        when(cache.getName()).thenReturn("properties");
        when(cache.getNativeCache()).thenReturn(new HashMap<>());

        // When & Then
        mockMvc.perform(get("/api/cache/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.properties.name").value("properties"))
                .andExpect(jsonPath("$.hotels.name").value("properties"))
                .andExpect(jsonPath("$.properties.nativeCache").exists())
                .andExpect(jsonPath("$.hotels.nativeCache").exists());

        verify(cacheManager).getCacheNames();
        verify(cacheManager, times(2)).getCache(anyString());
    }

    @Test
    void getCacheStatus_EmptyCaches() throws Exception {
        // Given
        when(cacheManager.getCacheNames()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/cache/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));

        verify(cacheManager).getCacheNames();
        verify(cacheManager, never()).getCache(anyString());
    }

    @Test
    void clearCache_Success() throws Exception {
        // Given
        String cacheName = "properties";
        doNothing().when(cacheService).clearCache(cacheName);

        // When & Then
        mockMvc.perform(post("/api/cache/clear/" + cacheName))
                .andExpect(status().isOk())
                .andExpect(content().string("Cache 'properties' cleared successfully"));

        verify(cacheService).clearCache(cacheName);
    }

    @Test
    void clearCache_Error() throws Exception {
        // Given
        String cacheName = "properties";
        doThrow(new RuntimeException("Cache error")).when(cacheService).clearCache(cacheName);

        // When & Then
        mockMvc.perform(post("/api/cache/clear/" + cacheName))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error clearing cache: Cache error"));

        verify(cacheService).clearCache(cacheName);
    }

    @Test
    void clearAllCaches_Success() throws Exception {
        // Given
        doNothing().when(cacheService).evictRelatedCaches("properties", "hotels", "reviews", "translations");

        // When & Then
        mockMvc.perform(post("/api/cache/clear/all"))
                .andExpect(status().isOk())
                .andExpect(content().string("All caches cleared successfully"));

        verify(cacheService).evictRelatedCaches("properties", "hotels", "reviews", "translations");
    }

    @Test
    void clearAllCaches_Error() throws Exception {
        // Given
        doThrow(new RuntimeException("Cache error")).when(cacheService).evictRelatedCaches("properties", "hotels", "reviews", "translations");

        // When & Then
        mockMvc.perform(post("/api/cache/clear/all"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error clearing caches: Cache error"));

        verify(cacheService).evictRelatedCaches("properties", "hotels", "reviews", "translations");
    }

    @Test
    void getCacheStats_Success() throws Exception {
        // Given
        when(cacheManager.getCacheNames()).thenReturn(Arrays.asList("properties", "hotels", "reviews"));

        // When & Then
        mockMvc.perform(get("/api/cache/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCaches").value(3))
                .andExpect(jsonPath("$.cacheNames").isArray())
                .andExpect(jsonPath("$.cacheNames[0]").value("properties"))
                .andExpect(jsonPath("$.cacheNames[1]").value("hotels"))
                .andExpect(jsonPath("$.cacheNames[2]").value("reviews"));

        verify(cacheManager).getCacheNames();
    }

    @Test
    void getCacheStats_EmptyCaches() throws Exception {
        // Given
        when(cacheManager.getCacheNames()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/cache/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCaches").value(0))
                .andExpect(jsonPath("$.cacheNames").isArray())
                .andExpect(jsonPath("$.cacheNames").isEmpty());

        verify(cacheManager).getCacheNames();
    }

    // Direct method tests for better coverage
    @Test
    void getCacheStatus_DirectMethod_Success() {
        // Given
        when(cacheManager.getCacheNames()).thenReturn(Arrays.asList("properties", "hotels"));
        when(cacheManager.getCache("properties")).thenReturn(cache);
        when(cacheManager.getCache("hotels")).thenReturn(cache);
        when(cache.getName()).thenReturn("properties");
        when(cache.getNativeCache()).thenReturn(new HashMap<>());

        // When
        ResponseEntity<Map<String, Object>> response = cacheController.getCacheStatus();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("properties");
        assertThat(response.getBody()).containsKey("hotels");
        
        Map<String, Object> propertiesCache = (Map<String, Object>) response.getBody().get("properties");
        assertThat(propertiesCache).containsKey("name");
        assertThat(propertiesCache).containsKey("nativeCache");
        
        verify(cacheManager).getCacheNames();
        verify(cacheManager, times(2)).getCache(anyString());
    }

    @Test
    void clearCache_DirectMethod_Success() {
        // Given
        String cacheName = "properties";
        doNothing().when(cacheService).clearCache(cacheName);

        // When
        ResponseEntity<String> response = cacheController.clearCache(cacheName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Cache 'properties' cleared successfully");
        verify(cacheService).clearCache(cacheName);
    }

    @Test
    void clearCache_DirectMethod_Error() {
        // Given
        String cacheName = "properties";
        doThrow(new RuntimeException("Cache error")).when(cacheService).clearCache(cacheName);

        // When
        ResponseEntity<String> response = cacheController.clearCache(cacheName);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Error clearing cache: Cache error");
        verify(cacheService).clearCache(cacheName);
    }

    @Test
    void clearAllCaches_DirectMethod_Success() {
        // Given
        doNothing().when(cacheService).evictRelatedCaches("properties", "hotels", "reviews", "translations");

        // When
        ResponseEntity<String> response = cacheController.clearAllCaches();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("All caches cleared successfully");
        verify(cacheService).evictRelatedCaches("properties", "hotels", "reviews", "translations");
    }

    @Test
    void clearAllCaches_DirectMethod_Error() {
        // Given
        doThrow(new RuntimeException("Cache error")).when(cacheService).evictRelatedCaches("properties", "hotels", "reviews", "translations");

        // When
        ResponseEntity<String> response = cacheController.clearAllCaches();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Error clearing caches: Cache error");
        verify(cacheService).evictRelatedCaches("properties", "hotels", "reviews", "translations");
    }

    @Test
    void getCacheStats_DirectMethod_Success() {
        // Given
        when(cacheManager.getCacheNames()).thenReturn(Arrays.asList("properties", "hotels", "reviews"));

        // When
        ResponseEntity<Map<String, Object>> response = cacheController.getCacheStats();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("totalCaches")).isEqualTo(3);
        assertThat(response.getBody().get("cacheNames")).isEqualTo(Arrays.asList("properties", "hotels", "reviews"));
        verify(cacheManager).getCacheNames();
    }
}
