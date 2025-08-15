package com.example.cupid.controller;

import com.example.cupid.entity.Property;
import com.example.cupid.repository.PropertyRepository;
import com.example.cupid.service.CupidFetchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PropertyControllerTest {

    @Mock
    private CupidFetchService fetchService;

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private PropertyController propertyController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Property testProperty;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(propertyController).build();
        objectMapper = new ObjectMapper();
        
        testProperty = new Property();
        testProperty.setHotelId(1270324L);
        testProperty.setName("Test Hotel");
        testProperty.setStars(4);
        testProperty.setRating(BigDecimal.valueOf(8.5));
        testProperty.setReviewCount(100);
        testProperty.setUpdatedAt(Instant.now());
    }

    @Test
    void importList_Success() throws Exception {
        // Given
        List<Long> hotelIds = Arrays.asList(1270324L, 67890L);
        String requestBody = objectMapper.writeValueAsString(hotelIds);

        // When
        mockMvc.perform(post("/api/properties/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .param("reviewsToFetch", "10"))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Imported 2 hotels"));

        // Then
        verify(fetchService, times(2)).fetchAndSave(anyLong(), eq(10));
    }

    @Test
    void importList_WithErrors() throws Exception {
        // Given
        List<Long> hotelIds = Arrays.asList(1270324L, 67890L);
        String requestBody = objectMapper.writeValueAsString(hotelIds);

        doNothing().when(fetchService).fetchAndSave(1270324L, 10);
        doThrow(new RuntimeException("Error")).when(fetchService).fetchAndSave(67890L, 10);

        // When
        mockMvc.perform(post("/api/properties/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .param("reviewsToFetch", "10"))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Imported 1 hotels, 1 failed"));

        // Then
        verify(fetchService, times(2)).fetchAndSave(anyLong(), eq(10));
    }

    @Test
    void importList_EmptyList() throws Exception {
        // Given
        List<Long> hotelIds = Arrays.asList();
        String requestBody = objectMapper.writeValueAsString(hotelIds);

        // When
        mockMvc.perform(post("/api/properties/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .param("reviewsToFetch", "10"))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Imported 0 hotels"));

        // Then
        verify(fetchService, never()).fetchAndSave(anyLong(), anyInt());
    }

    @Test
    void importList_DefaultReviewsToFetch() throws Exception {
        // Given
        List<Long> hotelIds = Arrays.asList(1270324L);
        String requestBody = objectMapper.writeValueAsString(hotelIds);

        // When
        mockMvc.perform(post("/api/properties/import")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Imported 1 hotels"));

        // Then
        verify(fetchService).fetchAndSave(1270324L, 10); // Default value
    }

    @Test
    void getProperty_Success() throws Exception {
        // Given
        when(propertyRepository.findById(1270324L)).thenReturn(Optional.of(testProperty));

        // When & Then
        mockMvc.perform(get("/api/properties/1270324"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotelId").value(1270324))
                .andExpect(jsonPath("$.name").value("Test Hotel"))
                .andExpect(jsonPath("$.stars").value(4))
                .andExpect(jsonPath("$.rating").value(8.5))
                .andExpect(jsonPath("$.reviewCount").value(100));

        verify(propertyRepository).findById(1270324L);
    }

    @Test
    void getProperty_NotFound() throws Exception {
        // Given
        when(propertyRepository.findById(1270324L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/properties/1270324"))
                .andExpect(status().isNotFound());

        verify(propertyRepository).findById(1270324L);
    }

    @Test
    void refreshProperty_Success() throws Exception {
        // Given
        doNothing().when(fetchService).fetchAndSave(1270324L, 20);

        // When & Then
        mockMvc.perform(post("/api/properties/1270324/refresh")
                .param("reviewsToFetch", "20"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        verify(fetchService).fetchAndSave(1270324L, 20);
    }

    @Test
    void refreshProperty_DefaultReviewsToFetch() throws Exception {
        // Given
        doNothing().when(fetchService).fetchAndSave(1270324L, 20);

        // When & Then
        mockMvc.perform(post("/api/properties/1270324/refresh"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        verify(fetchService).fetchAndSave(1270324L, 20); // Default value
    }

    @Test
    void refreshProperty_Error() throws Exception {
        // Given
        doThrow(new RuntimeException("Service error")).when(fetchService).fetchAndSave(1270324L, 20);

        // When & Then
        mockMvc.perform(post("/api/properties/1270324/refresh")
                .param("reviewsToFetch", "20"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error: Service error"));

        verify(fetchService).fetchAndSave(1270324L, 20);
    }

    @Test
    void refreshProperty_CustomReviewsToFetch() throws Exception {
        // Given
        doNothing().when(fetchService).fetchAndSave(1270324L, 50);

        // When & Then
        mockMvc.perform(post("/api/properties/1270324/refresh")
                .param("reviewsToFetch", "50"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        verify(fetchService).fetchAndSave(1270324L, 50);
    }

    // Direct method tests for better coverage
    @Test
    void importList_DirectMethod_Success() {
        // Given
        List<Long> hotelIds = Arrays.asList(1270324L, 67890L);

        // When
        ResponseEntity<?> response = propertyController.importList(hotelIds, 10);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo("Imported 2 hotels");
        verify(fetchService, times(2)).fetchAndSave(anyLong(), eq(10));
    }

    @Test
    void importList_DirectMethod_WithErrors() {
        // Given
        List<Long> hotelIds = Arrays.asList(1270324L, 67890L);
        doNothing().when(fetchService).fetchAndSave(1270324L, 10);
        doThrow(new RuntimeException("Error")).when(fetchService).fetchAndSave(67890L, 10);

        // When
        ResponseEntity<?> response = propertyController.importList(hotelIds, 10);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isEqualTo("Imported 1 hotels, 1 failed");
        verify(fetchService, times(2)).fetchAndSave(anyLong(), eq(10));
    }

    @Test
    void getProperty_DirectMethod_Success() {
        // Given
        when(propertyRepository.findById(1270324L)).thenReturn(Optional.of(testProperty));

        // When
        ResponseEntity<Property> response = propertyController.getProperty(1270324L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testProperty);
        verify(propertyRepository).findById(1270324L);
    }

    @Test
    void getProperty_DirectMethod_NotFound() {
        // Given
        when(propertyRepository.findById(1270324L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Property> response = propertyController.getProperty(1270324L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
        verify(propertyRepository).findById(1270324L);
    }

    @Test
    void refreshProperty_DirectMethod_Success() {
        // Given
        doNothing().when(fetchService).fetchAndSave(1270324L, 20);

        // When
        ResponseEntity<?> response = propertyController.refreshProperty(1270324L, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("ok");
        verify(fetchService).fetchAndSave(1270324L, 20);
    }

    @Test
    void refreshProperty_DirectMethod_Error() {
        // Given
        doThrow(new RuntimeException("Service error")).when(fetchService).fetchAndSave(1270324L, 20);

        // When
        ResponseEntity<?> response = propertyController.refreshProperty(1270324L, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Error: Service error");
        verify(fetchService).fetchAndSave(1270324L, 20);
    }
}
