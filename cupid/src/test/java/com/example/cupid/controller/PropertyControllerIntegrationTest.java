package com.example.cupid.controller;

import com.example.cupid.config.TestServiceConfig;
import com.example.cupid.entity.Property;
import com.example.cupid.repository.PropertyRepository;
import com.example.cupid.service.CupidFetchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class PropertyControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private PropertyRepository propertyRepository;

    @MockBean
    private CupidFetchService cupidFetchService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Property testProperty;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        // Create test property
        testProperty = new Property();
        testProperty.setHotelId(1270324L);
        testProperty.setName("Test Hotel");
        testProperty.setStars(4);
        testProperty.setRating(BigDecimal.valueOf(8.5));
        testProperty.setReviewCount(100);
        testProperty.setUpdatedAt(Instant.now());
        
        // Save to database
        propertyRepository.save(testProperty);
        
        // Setup CupidFetchService mock behavior
        // Default behavior: do nothing (success case)
        doNothing().when(cupidFetchService).fetchAndSave(anyLong(), anyInt());
        
        // For the refreshProperty_NotFound test, throw an exception for hotel ID 986622
        doThrow(new RuntimeException("Hotel not found"))
            .when(cupidFetchService).fetchAndSave(eq(986622L), anyInt());
    }

    @Test
    void getProperty_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/properties/1270324"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotelId").value(1270324L))
                .andExpect(jsonPath("$.name").value("Test Hotel"))
                .andExpect(jsonPath("$.stars").value(4))
                .andExpect(jsonPath("$.rating").value(8.5))
                .andExpect(jsonPath("$.reviewCount").value(100));
    }

    @Test
    void getProperty_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/properties/986622"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProperty_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/properties/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importList_Success() throws Exception {
        // Given
        List<Long> hotelIds = Arrays.asList(1270324L, 67890L);
        String requestBody = objectMapper.writeValueAsString(hotelIds);

        // When & Then
        mockMvc.perform(post("/api/properties/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .param("reviewsToFetch", "10"))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Imported 2 hotels"));
    }

    @Test
    void importList_EmptyList() throws Exception {
        // Given
        List<Long> hotelIds = Arrays.asList();
        String requestBody = objectMapper.writeValueAsString(hotelIds);

        // When & Then
        mockMvc.perform(post("/api/properties/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .param("reviewsToFetch", "10"))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Imported 0 hotels"));
    }

    @Test
    void importList_DefaultReviewsToFetch() throws Exception {
        // Given
        List<Long> hotelIds = Arrays.asList(1270324L);
        String requestBody = objectMapper.writeValueAsString(hotelIds);

        // When & Then
        mockMvc.perform(post("/api/properties/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Imported 1 hotels"));
    }

    @Test
    void importList_InvalidJson() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/api/properties/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
                .param("reviewsToFetch", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshProperty_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/properties/1270324/refresh")
                .param("reviewsToFetch", "20"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void refreshProperty_DefaultReviewsToFetch() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/properties/1270324/refresh"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void refreshProperty_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/properties/986622/refresh")
                .param("reviewsToFetch", "20"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void refreshProperty_InvalidId() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/properties/invalid/refresh")
                .param("reviewsToFetch", "20"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshProperty_InvalidReviewsToFetch() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/properties/1270324L/refresh")
                .param("reviewsToFetch", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void databaseIntegration_SaveAndRetrieve() {
        // Given
        Property newProperty = new Property();
        newProperty.setHotelId(54321L);
        newProperty.setName("New Test Hotel");
        newProperty.setStars(5);
        newProperty.setRating(BigDecimal.valueOf(9.0));
        newProperty.setReviewCount(200);
        newProperty.setUpdatedAt(Instant.now());

        // When
        Property savedProperty = propertyRepository.save(newProperty);
        Property retrievedProperty = propertyRepository.findById(savedProperty.getHotelId()).orElse(null);

        // Then
        assertThat(savedProperty).isNotNull();
        assertThat(savedProperty.getHotelId()).isNotNull();
        assertThat(retrievedProperty).isNotNull();
        assertThat(retrievedProperty.getHotelId()).isEqualTo(54321L);
        assertThat(retrievedProperty.getName()).isEqualTo("New Test Hotel");
        assertThat(retrievedProperty.getStars()).isEqualTo(5);
        assertThat(retrievedProperty.getRating()).isEqualTo(BigDecimal.valueOf(9.0));
        assertThat(retrievedProperty.getReviewCount()).isEqualTo(200);
    }

    @Test
    void databaseIntegration_FindByHotelId() {
        // Given
        Property property = new Property();
        property.setHotelId(98765L);
        property.setName("Find Test Hotel");
        property.setStars(3);
        property.setRating(BigDecimal.valueOf(7.5));
        property.setReviewCount(50);
        property.setUpdatedAt(Instant.now());
        propertyRepository.save(property);

        // When
        Property foundProperty = propertyRepository.findByHotelId(98765L).orElse(null);

        // Then
        assertThat(foundProperty).isNotNull();
        assertThat(foundProperty.getHotelId()).isEqualTo(98765L);
        assertThat(foundProperty.getName()).isEqualTo("Find Test Hotel");
    }

    @Test
    void databaseIntegration_UpdateProperty() {
        // Given
        Property property = propertyRepository.findById(testProperty.getHotelId()).orElse(null);
        assertThat(property).isNotNull();

        // When
        property.setName("Updated Hotel Name");
        property.setStars(5);
        Property updatedProperty = propertyRepository.save(property);

        // Then
        assertThat(updatedProperty.getName()).isEqualTo("Updated Hotel Name");
        assertThat(updatedProperty.getStars()).isEqualTo(5);

        // Verify in database
        Property retrievedProperty = propertyRepository.findById(testProperty.getHotelId()).orElse(null);
        assertThat(retrievedProperty).isNotNull();
        assertThat(retrievedProperty.getName()).isEqualTo("Updated Hotel Name");
        assertThat(retrievedProperty.getStars()).isEqualTo(5);
    }

    @Test
    void databaseIntegration_DeleteProperty() {
        // Given
        Property propertyToDelete = new Property();
        propertyToDelete.setHotelId(11111L);
        propertyToDelete.setName("Delete Test Hotel");
        propertyToDelete.setStars(2);
        propertyToDelete.setRating(BigDecimal.valueOf(6.0));
        propertyToDelete.setReviewCount(25);
        propertyToDelete.setUpdatedAt(Instant.now());
        Property savedProperty = propertyRepository.save(propertyToDelete);

        // Verify it exists
        assertThat(propertyRepository.findById(savedProperty.getHotelId())).isPresent();

        // When
        propertyRepository.deleteById(savedProperty.getHotelId());

        // Then
        assertThat(propertyRepository.findById(savedProperty.getHotelId())).isEmpty();
    }
}
