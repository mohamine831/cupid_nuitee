package com.example.cupid.service;

import com.example.cupid.api.v1.V1Mapper;
import com.example.cupid.api.v1.dto.PropertyDto;
import com.example.cupid.entity.Property;
import com.example.cupid.repository.PropertyRepository;
import com.example.cupid.repository.ReviewRepository;
import com.example.cupid.repository.PropertyTranslationRepository;
import com.example.cupid.utils.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PropertyTranslationRepository translationRepository;

    @Mock
    private V1Mapper mapper;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private PropertyService propertyService;

    private Property testProperty;
    private PropertyDto testPropertyDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testProperty = TestDataBuilder.createTestProperty(1270324L, "Test Hotel", "Test City", 4, BigDecimal.valueOf(8.5), 100);

        // TODO: Consider creating a TestDataBuilder for DTOs to avoid this long constructor
        testPropertyDto = new PropertyDto(
                1270324L, null, "Test Hotel", null, null, null, null, null, null, null,
                4, BigDecimal.valueOf(8.5), 100, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getHotels_Success() {
        // Given
        List<Property> properties = List.of(testProperty);
        Page<Property> propertyPage = new PageImpl<>(properties, pageable, 1);
        when(propertyRepository.findAllWithBasicDetails(pageable)).thenReturn(propertyPage);
        when(mapper.toDto((Property) testProperty)).thenReturn(testPropertyDto);

        // When
        Page<PropertyDto> result = propertyService.getHotels(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getHotelId()).isEqualTo(1270324L);
        verify(propertyRepository).findAllWithBasicDetails(pageable);
        verify(mapper).toDto((Property) testProperty);
    }

    @Test
    void getHotels_EmptyPage() {
        // Given
        Page<Property> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(propertyRepository.findAllWithBasicDetails(pageable)).thenReturn(emptyPage);

        // When
        Page<PropertyDto> result = propertyService.getHotels(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(propertyRepository).findAllWithBasicDetails(pageable);
        verify(mapper, never()).toDto(any(Property.class));
    }

    @Test
    void getHotelById_Success() {
        // Given
        when(propertyRepository.findByIdWithAllDetails(1270324L)).thenReturn(Optional.of(testProperty));
        when(mapper.toDto((Property) testProperty)).thenReturn(testPropertyDto);

        // When
        Optional<PropertyDto> result = propertyService.getHotelById(1270324L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getHotelId()).isEqualTo(1270324L);
        verify(propertyRepository).findByIdWithAllDetails(1270324L);
        verify(mapper).toDto((Property) testProperty);
    }

    @Test
    void getHotelById_NotFound() {
        // Given
        when(propertyRepository.findByIdWithAllDetails(1270324L)).thenReturn(Optional.empty());

        // When
        Optional<PropertyDto> result = propertyService.getHotelById(1270324L);

        // Then
        assertThat(result).isEmpty();
        verify(propertyRepository).findByIdWithAllDetails(1270324L);
        verify(mapper, never()).toDto(any(Property.class));
    }

    @Test
    void getHotelByHotelId_Success() {
        // Given
        when(propertyRepository.findByHotelId(1270324L)).thenReturn(Optional.of(testProperty));
        when(mapper.toDto((Property) testProperty)).thenReturn(testPropertyDto);

        // When
        Optional<PropertyDto> result = propertyService.getHotelByHotelId(1270324L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getHotelId()).isEqualTo(1270324L);
        verify(propertyRepository).findByHotelId(1270324L);
        verify(mapper).toDto((Property) testProperty);
    }

    @Test
    void getHotelByHotelId_NotFound() {
        // Given
        when(propertyRepository.findByHotelId(1270324L)).thenReturn(Optional.empty());

        // When
        Optional<PropertyDto> result = propertyService.getHotelByHotelId(1270324L);

        // Then
        assertThat(result).isEmpty();
        verify(propertyRepository).findByHotelId(1270324L);
        verify(mapper, never()).toDto(any(Property.class));
    }

    @Test
    void searchHotelsByNameAndCity_WithCity() {
        // Given
        String name = "Hotel";
        String city = "Paris";
        List<Property> properties = List.of(testProperty);
        when(propertyRepository.searchByNameAndCityNative("%Hotel%", "%Paris%")).thenReturn(properties);
        when(mapper.toDto((Property) testProperty)).thenReturn(testPropertyDto);

        // When
        List<PropertyDto> result = propertyService.searchHotelsByNameAndCity(name, city);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHotelId()).isEqualTo(1270324L);
        verify(propertyRepository).searchByNameAndCityNative("%Hotel%", "%Paris%");
        verify(propertyRepository, never()).searchByNameAndCity(anyString(), anyString());
        verify(mapper).toDto((Property) testProperty);
    }

    @Test
    void searchHotelsByNameAndCity_WithoutCity() {
        // Given
        String name = "Hotel";
        String city = null;
        List<Property> properties = List.of(testProperty);
        when(propertyRepository.searchByNameAndCity("%Hotel%", null)).thenReturn(properties);
        when(mapper.toDto(testProperty)).thenReturn(testPropertyDto);

        // When
        List<PropertyDto> result = propertyService.searchHotelsByNameAndCity(name, city);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHotelId()).isEqualTo(1270324L);
        verify(propertyRepository).searchByNameAndCity("%Hotel%", null);
        verify(propertyRepository, never()).searchByNameAndCityNative(anyString(), anyString());
        verify(mapper).toDto(testProperty);
    }

    @Test
    void searchHotelsByNameAndCity_EmptyCity() {
        // Given
        String name = "Hotel";
        String city = "";
        List<Property> properties = List.of(testProperty);
        when(propertyRepository.searchByNameAndCity("%Hotel%", "")).thenReturn(properties);
        when(mapper.toDto(testProperty)).thenReturn(testPropertyDto);

        // When
        List<PropertyDto> result = propertyService.searchHotelsByNameAndCity(name, city);

        // Then
        assertThat(result).hasSize(1);
        verify(propertyRepository).searchByNameAndCity("%Hotel%", "");
        verify(propertyRepository, never()).searchByNameAndCityNative(anyString(), anyString());
    }

    @Test
    void searchHotelsByName_Success() {
        // Given
        String name = "Hotel";
        List<Property> properties = List.of(testProperty);
        when(propertyRepository.searchByName("%Hotel%")).thenReturn(properties);
        when(mapper.toDto(testProperty)).thenReturn(testPropertyDto);

        // When
        List<PropertyDto> result = propertyService.searchHotelsByName(name);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHotelId()).isEqualTo(1270324L);
        verify(propertyRepository).searchByName("%Hotel%");
        verify(mapper).toDto(testProperty);
    }

    @Test
    void searchHotelsByName_NoResults() {
        // Given
        String name = "NonExistent";
        when(propertyRepository.searchByName("%NonExistent%")).thenReturn(List.of());

        // When
        List<PropertyDto> result = propertyService.searchHotelsByName(name);

        // Then
        assertThat(result).isEmpty();
        verify(propertyRepository).searchByName("%NonExistent%");
        verify(mapper, never()).toDto(any(Property.class));
    }

    @Test
    void getHotelReviews_ReturnsEmptyList() {
        // Given
        Long hotelId = 1270324L;

        // When
        List<PropertyDto> result = propertyService.getHotelReviews(hotelId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getHotelTranslations_ReturnsEmptyList() {
        // Given
        Long hotelId = 1270324L;

        // When
        List<PropertyDto> result = propertyService.getHotelTranslations(hotelId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void refreshProperty_Success() {
        // Given
        Long hotelId = 1270324L;
        doNothing().when(cacheService).evictRelatedCaches(any(String[].class));

        // When
        propertyService.refreshProperty(hotelId);

        // Then
        verify(cacheService).evictRelatedCaches("properties", "hotels", "reviews", "translations");
    }

    @Test
    void updateProperty_Success() {
        // Given
        Long hotelId = 1270324L;

        // When
        propertyService.updateProperty(hotelId);

        // Then
        // This method is currently empty, so we just verify it doesn't throw an exception
        assertThat(propertyService).isNotNull();
    }

    @Test
    void clearAllCaches_Success() {
        // Given
        doNothing().when(cacheService).evictRelatedCaches(any(String[].class));
        
        // When
        propertyService.clearAllCaches();

        // Then
        verify(cacheService).evictRelatedCaches("properties", "hotels", "reviews", "translations");
    }
}
