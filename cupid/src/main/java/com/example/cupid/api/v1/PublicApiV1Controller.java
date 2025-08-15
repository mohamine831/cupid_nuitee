package com.example.cupid.api.v1;

import com.example.cupid.api.v1.dto.PropertyDto;
import com.example.cupid.api.v1.dto.ReviewDto;
import com.example.cupid.api.v1.dto.PropertyTranslationDto;
import com.example.cupid.service.PropertyService;
import com.example.cupid.service.ReviewService;
import com.example.cupid.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PublicApiV1Controller {

    private final PropertyService propertyService;
    private final ReviewService reviewService;
    private final TranslationService translationService;

    @GetMapping("/hotels")
    public ResponseEntity<Page<PropertyDto>> getHotels(Pageable pageable) {
        log.debug("API call: getHotels with pageable: {}", pageable);
        try {
            Page<PropertyDto> hotels = propertyService.getHotels(pageable);
            return ResponseEntity.ok(hotels);
        } catch (Exception e) {
            log.error("Error fetching hotels", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<PropertyDto> getHotelById(@PathVariable Long hotelId) {
        log.debug("API call: getHotelById with hotelId: {}", hotelId);
        try {
            return propertyService.getHotelById(hotelId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching hotel by ID: {}", hotelId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/hotels/{hotelId}/reviews")
    public ResponseEntity<List<ReviewDto>> getReviewsByHotelId(@PathVariable Long hotelId) {
        log.debug("API call: getReviewsByHotelId with hotelId: {}", hotelId);
        try {
            List<ReviewDto> reviews = reviewService.getReviewsByHotelId(hotelId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching reviews for hotel: {}", hotelId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/hotels/{hotelId}/reviews/top")
    public ResponseEntity<List<ReviewDto>> getTopReviewsByHotelId(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "5") int limit) {
        log.debug("API call: getTopReviewsByHotelId with hotelId: {} and limit: {}", hotelId, limit);
        try {
            List<ReviewDto> reviews = reviewService.getTopReviewsByHotelId(hotelId, limit);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching top reviews for hotel: {}", hotelId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/hotels/{hotelId}/reviews/recent")
    public ResponseEntity<List<ReviewDto>> getRecentReviewsByHotelId(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "10") int limit) {
        log.debug("API call: getRecentReviewsByHotelId with hotelId: {} and limit: {}", hotelId, limit);
        try {
            List<ReviewDto> reviews = reviewService.getRecentReviewsByHotelId(hotelId, limit);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error fetching recent reviews for hotel: {}", hotelId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/hotels/{hotelId}/translations")
    public ResponseEntity<List<PropertyTranslationDto>> getTranslationsByHotelId(@PathVariable Long hotelId) {
        log.debug("API call: getTranslationsByHotelId with hotelId: {}", hotelId);
        try {
            List<PropertyTranslationDto> translations = translationService.getTranslationsByHotelId(hotelId);
            return ResponseEntity.ok(translations);
        } catch (Exception e) {
            log.error("Error fetching translations for hotel: {}", hotelId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/hotels/{hotelId}/translations/{lang}")
    public ResponseEntity<PropertyTranslationDto> getTranslationByHotelIdAndLang(
            @PathVariable Long hotelId,
            @PathVariable String lang) {
        log.debug("API call: getTranslationByHotelIdAndLang with hotelId: {} and lang: {}", hotelId, lang);
        try {
            return translationService.getTranslationByHotelIdAndLang(hotelId, lang)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching translation for hotel: {} and language: {}", hotelId, lang, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/hotels/search")
    public ResponseEntity<List<PropertyDto>> searchHotels(
            @RequestParam String name,
            @RequestParam(required = false) String city) {
        log.debug("API call: searchHotels with name: {} and city: {}", name, city);
        try {
            List<PropertyDto> hotels;
            if (city != null && !city.trim().isEmpty()) {
                hotels = propertyService.searchHotelsByNameAndCity(name, city);
            } else {
                hotels = propertyService.searchHotelsByName(name);
            }
            return ResponseEntity.ok(hotels);
        } catch (Exception e) {
            log.error("Error searching hotels with name: {} and city: {}", name, city, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/hotels/search/name")
    public ResponseEntity<List<PropertyDto>> searchHotelsByName(
            @RequestParam String name) {
        log.debug("API call: searchHotelsByName with name: {}", name);
        try {
            List<PropertyDto> hotels = propertyService.searchHotelsByName(name);
            return ResponseEntity.ok(hotels);
        } catch (Exception e) {
            log.error("Error searching hotels by name: {}", name, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<String> clearAllCaches() {
        log.info("API call: clearAllCaches");
        try {
            propertyService.clearAllCaches();
            return ResponseEntity.ok("All caches cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing caches", e);
            return ResponseEntity.internalServerError().body("Error clearing caches: " + e.getMessage());
        }
    }
}
