package com.example.cupid.service;

import com.example.cupid.api.v1.dto.PropertyDto;
import com.example.cupid.entity.Property;
import com.example.cupid.repository.PropertyRepository;
import com.example.cupid.repository.ReviewRepository;
import com.example.cupid.repository.PropertyTranslationRepository;
import com.example.cupid.api.v1.V1Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final ReviewRepository reviewRepository;
    private final PropertyTranslationRepository translationRepository;
    private final V1Mapper mapper;
    private final CacheService cacheService;

    @Cacheable(value = "hotels", key = "'page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PropertyDto> getHotels(Pageable pageable) {
        log.debug("Fetching hotels with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Property> properties = propertyRepository.findAllWithBasicDetails(pageable);
        return properties.map(mapper::toDto);
    }

    @Cacheable(value = "properties", key = "#hotelId")
    public Optional<PropertyDto> getHotelById(Long hotelId) {
        log.debug("Fetching hotel by ID: {}", hotelId);
        return propertyRepository.findByIdWithAllDetails(hotelId)
                .map(mapper::toDto);
    }

    @Cacheable(value = "properties", key = "'hotel_' + #hotelId")
    public Optional<PropertyDto> getHotelByHotelId(Long hotelId) {
        log.debug("Fetching hotel by hotel ID: {}", hotelId);
        return propertyRepository.findByHotelId(hotelId)
                .map(mapper::toDto);
    }

    @Cacheable(value = "properties", key = "'search_' + #name + '_' + #city")
    public List<PropertyDto> searchHotelsByNameAndCity(String name, String city) {
        log.debug("Searching hotels by name: {} and city: {}", name, city);
        
        List<Property> properties;
        
        if (city != null && !city.trim().isEmpty()) {
            // Use native query for efficient JSONB search when city is provided
            String nameWithWildcards = "%" + name + "%";
            String cityWithWildcards = "%" + city + "%";
            properties = propertyRepository.searchByNameAndCityNative(nameWithWildcards, cityWithWildcards);
        } else {
            // Use JPQL query when only name is provided
            String nameWithWildcards = "%" + name + "%";
            properties = propertyRepository.searchByNameAndCity(nameWithWildcards, city);
        }
        
        return properties.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Cacheable(value = "properties", key = "'search_name_' + #name")
    public List<PropertyDto> searchHotelsByName(String name) {
        log.debug("Searching hotels by name: {}", name);
        String nameWithWildcards = "%" + name + "%";
        List<Property> properties = propertyRepository.searchByName(nameWithWildcards);
        return properties.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Cacheable(value = "reviews", key = "'hotel_' + #hotelId")
    public List<PropertyDto> getHotelReviews(Long hotelId) {
        log.debug("Fetching reviews for hotel: {}", hotelId);
        // This would return reviews, but keeping the method signature for now
        // You might want to create a separate ReviewService
        return List.of();
    }

    @Cacheable(value = "translations", key = "'hotel_' + #hotelId")
    public List<PropertyDto> getHotelTranslations(Long hotelId) {
        log.debug("Fetching translations for hotel: {}", hotelId);
        // This would return translations, but keeping the method signature for now
        // You might want to create a separate TranslationService
        return List.of();
    }

    @Transactional
    @CacheEvict(value = {"properties", "hotels", "reviews", "translations"}, allEntries = true)
    public void refreshProperty(Long hotelId) {
        log.info("Refreshing property with hotel ID: {}", hotelId);
        // This method would trigger a refresh of the property data
        // and evict all related caches
        cacheService.evictRelatedCaches("properties", "hotels", "reviews", "translations");
    }

    @Transactional
    @CacheEvict(value = {"properties", "hotels"}, key = "#hotelId")
    public void updateProperty(Long hotelId) {
        log.info("Updating property with hotel ID: {}", hotelId);
        // This method would update the property data
        // and evict specific caches
    }

    public void clearAllCaches() {
        log.info("Clearing all caches");
        cacheService.evictRelatedCaches("properties", "hotels", "reviews", "translations");
    }
}
