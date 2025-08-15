package com.example.cupid.config;

import com.example.cupid.service.CupidFetchService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.*;

@TestConfiguration
@Profile("test")
public class TestServiceConfig {

    @Bean
    @Primary
    public CupidFetchService mockCupidFetchService() {
        CupidFetchService mockService = mock(CupidFetchService.class);
        
        // Default behavior: do nothing (success case)
        doNothing().when(mockService).fetchAndSave(anyLong(), anyInt());
        
        // For the refreshProperty_NotFound test, throw an exception for hotel ID 986622
        doThrow(new RuntimeException("Hotel not found"))
            .when(mockService).fetchAndSave(eq(986622L), anyInt());
        
        return mockService;
    }
}
