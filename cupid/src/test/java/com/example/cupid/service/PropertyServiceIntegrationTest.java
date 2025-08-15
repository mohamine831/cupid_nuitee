package com.example.cupid.service;

import com.example.cupid.api.v1.V1Mapper;
import com.example.cupid.api.v1.dto.PropertyDto;
import com.example.cupid.entity.Property;
import com.example.cupid.repository.PropertyRepository;
import com.example.cupid.repository.ReviewRepository;
import com.example.cupid.repository.PropertyTranslationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PropertyServiceIntegrationTest {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PropertyTranslationRepository translationRepository;

    @Autowired
    private V1Mapper mapper;

    @Autowired
    private CacheService cacheService;

    private Property testProperty1;
    private Property testProperty2;
    private Property testProperty3;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        cacheService.evictRelatedCaches("properties", "hotels", "reviews", "translations");

        // Create test properties with minimal required fields
        testProperty1 = createTestProperty(1270324L, "Test Hotel 1", "Paris", 4, BigDecimal.valueOf(8.5), 100);
        testProperty2 = createTestProperty(67890L, "Test Hotel 2", "London", 5, BigDecimal.valueOf(9.0), 200);
        testProperty3 = createTestProperty(11111L, "Another Hotel", "Paris", 3, BigDecimal.valueOf(7.5), 50);

        // Save to database
        propertyRepository.save(testProperty1);
        propertyRepository.save(testProperty2);
        propertyRepository.save(testProperty3);
    }

    private Property createTestProperty(Long hotelId, String name, String city, Integer stars, BigDecimal rating, Integer reviewCount) {
        Property property = new Property();
        property.setHotelId(hotelId);
        property.setName(name);
        property.setStars(stars);
        property.setRating(rating);
        property.setReviewCount(reviewCount);
        property.setUpdatedAt(Instant.now());
        
        // Set address JSON for city-based searches
        String addressJson = String.format("{\"city\": \"%s\", \"country\": \"Test Country\"}", city);
        property.setAddressJson(addressJson);
        
        // Initialize empty lists to avoid null pointer issues
        property.setPhotos(List.of());
        property.setFacilities(List.of());
        property.setRooms(List.of());
        property.setPolicies(List.of());
        property.setReviews(List.of());
        property.setTranslations(List.of());
        
        return property;
    }

    @Test
    void getHotels_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<PropertyDto> result = propertyService.getHotels(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(1);
        
        // Verify properties are mapped correctly
        List<PropertyDto> properties = result.getContent();
        assertThat(properties).anyMatch(p -> p.getHotelId().equals(1270324L) && p.getName().equals("Test Hotel 1"));
        assertThat(properties).anyMatch(p -> p.getHotelId().equals(67890L) && p.getName().equals("Test Hotel 2"));
        assertThat(properties).anyMatch(p -> p.getHotelId().equals(11111L) && p.getName().equals("Another Hotel"));
    }

    @Test
    void getHotels_Pagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<PropertyDto> result = propertyService.getHotels(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    void getHotels_EmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(10, 10); // Page that doesn't exist

        // When
        Page<PropertyDto> result = propertyService.getHotels(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    void getHotelById_Success() {
        // When
        Optional<PropertyDto> result = propertyService.getHotelById(1270324L);

        // Then
        assertThat(result).isPresent();
        PropertyDto property = result.get();
        assertThat(property.getHotelId()).isEqualTo(1270324L);
        assertThat(property.getName()).isEqualTo("Test Hotel 1");
        assertThat(property.getStars()).isEqualTo(4);
        assertThat(property.getRating()).isEqualTo(BigDecimal.valueOf(8.5));
        assertThat(property.getReviewCount()).isEqualTo(100);
    }

    @Test
    void getHotelById_NotFound() {
        // When
        Optional<PropertyDto> result = propertyService.getHotelById(986622L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHotelByHotelId_Success() {
        // When
        Optional<PropertyDto> result = propertyService.getHotelByHotelId(1270324L);

        // Then
        assertThat(result).isPresent();
        PropertyDto property = result.get();
        assertThat(property.getHotelId()).isEqualTo(1270324L);
        assertThat(property.getName()).isEqualTo("Test Hotel 1");
    }

    @Test
    void getHotelByHotelId_NotFound() {
        // When
        Optional<PropertyDto> result = propertyService.getHotelByHotelId(986622L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void searchHotelsByName_Success() {
        // When
        List<PropertyDto> result = propertyService.searchHotelsByName("Test");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).anyMatch(p -> p.getName().equals("Test Hotel 1"));
        assertThat(result).anyMatch(p -> p.getName().equals("Test Hotel 2"));
    }

    @Test
    void searchHotelsByName_NoResults() {
        // When
        List<PropertyDto> result = propertyService.searchHotelsByName("NonExistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void searchHotelsByNameAndCity_WithCity() {
        // When
        List<PropertyDto> result = propertyService.searchHotelsByNameAndCity("Hotel", "Paris");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).anyMatch(p -> p.getName().equals("Test Hotel 1"));
        assertThat(result).anyMatch(p -> p.getName().equals("Another Hotel"));
    }

    @Test
    void searchHotelsByNameAndCity_WithoutCity() {
        // When
        List<PropertyDto> result = propertyService.searchHotelsByNameAndCity("Hotel", null);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).anyMatch(p -> p.getName().equals("Test Hotel 1"));
        assertThat(result).anyMatch(p -> p.getName().equals("Test Hotel 2"));
        assertThat(result).anyMatch(p -> p.getName().equals("Another Hotel"));
    }

    @Test
    void searchHotelsByNameAndCity_EmptyCity() {
        // When
        List<PropertyDto> result = propertyService.searchHotelsByNameAndCity("Hotel", "");

        // Then
        assertThat(result).hasSize(3);
    }

    @Test
    void searchHotelsByNameAndCity_NoResults() {
        // When
        List<PropertyDto> result = propertyService.searchHotelsByNameAndCity("NonExistent", "Paris");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHotelReviews_ReturnsEmptyList() {
        // When
        List<PropertyDto> result = propertyService.getHotelReviews(1270324L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHotelTranslations_ReturnsEmptyList() {
        // When
        List<PropertyDto> result = propertyService.getHotelTranslations(1270324L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void refreshProperty_Success() {
        // When
        propertyService.refreshProperty(1270324L);

        // Then
        // Method should complete without throwing exception
        assertThat(propertyService).isNotNull();
    }

    @Test
    void updateProperty_Success() {
        // When
        propertyService.updateProperty(1270324L);

        // Then
        // Method should complete without throwing exception
        assertThat(propertyService).isNotNull();
    }

    @Test
    void clearAllCaches_Success() {
        // When
        propertyService.clearAllCaches();

        // Then
        // Method should complete without throwing exception
        assertThat(propertyService).isNotNull();
    }

    @Test
    void caching_Integration() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When - First call should populate cache
        Page<PropertyDto> result1 = propertyService.getHotels(pageable);
        
        // Then
        assertThat(result1).isNotNull();
        assertThat(result1.getContent()).hasSize(3);

        // When - Second call should use cache
        Page<PropertyDto> result2 = propertyService.getHotels(pageable);
        
        // Then
        assertThat(result2).isNotNull();
        assertThat(result2.getContent()).hasSize(3);
        assertThat(result2.getContent()).isEqualTo(result1.getContent());
    }

    @Test
    void databaseTransaction_Integration() {
        // Given
        Property newProperty = createTestProperty(986622L, "Transaction Test Hotel", "Berlin", 4, BigDecimal.valueOf(8.0), 75);

        // When
        Property savedProperty = propertyRepository.save(newProperty);
        
        // Then
        assertThat(savedProperty).isNotNull();
        assertThat(savedProperty.getHotelId()).isEqualTo(986622L);

        // Verify it can be retrieved
        Optional<Property> retrievedProperty = propertyRepository.findById(986622L);
        assertThat(retrievedProperty).isPresent();
        assertThat(retrievedProperty.get().getName()).isEqualTo("Transaction Test Hotel");

        // Test service method with new property
        Optional<PropertyDto> serviceResult = propertyService.getHotelById(986622L);
        assertThat(serviceResult).isPresent();
        assertThat(serviceResult.get().getName()).isEqualTo("Transaction Test Hotel");
    }
}
