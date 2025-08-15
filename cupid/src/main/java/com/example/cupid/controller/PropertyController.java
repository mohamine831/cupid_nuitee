package com.example.cupid.controller;

import com.example.cupid.entity.Property;
import com.example.cupid.repository.PropertyRepository;
import com.example.cupid.service.CupidFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Slf4j
public class PropertyController {
    private final CupidFetchService fetchService;
    private final PropertyRepository propertyRepository;

    @PostMapping("/import")
    public ResponseEntity<?> importList(@RequestBody List<Long> hotelIds,
                                        @RequestParam(defaultValue = "10") int reviewsToFetch) {
        log.info("Starting import for {} hotels: {}", hotelIds.size(), hotelIds);
        
        int successCount = 0;
        int errorCount = 0;
        
        for (Long id : hotelIds) {
            try {
                log.info("Processing hotel ID: {}", id);
                fetchService.fetchAndSave(id, reviewsToFetch);
                successCount++;
                log.info("Successfully processed hotel ID: {}", id);
            } catch (Exception e) {
                errorCount++;
                log.error("Error processing hotel ID: {}", id, e);
            }
        }
        
        log.info("Import completed. Success: {}, Errors: {}", successCount, errorCount);
        
        if (errorCount > 0) {
            return ResponseEntity.accepted().body(String.format("Imported %d hotels, %d failed", successCount, errorCount));
        } else {
            return ResponseEntity.accepted().body(String.format("Imported %d hotels", successCount));
        }
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<Property> getProperty(@PathVariable Long hotelId) {
        return propertyRepository.findById(hotelId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{hotelId}/refresh")
    public ResponseEntity<?> refreshProperty(@PathVariable Long hotelId,
                                             @RequestParam(defaultValue = "20") int reviewsToFetch) {
        try {
            fetchService.fetchAndSave(hotelId, reviewsToFetch);
            return ResponseEntity.ok().body("ok");
        } catch (Exception e) {
            log.error("Error refreshing property with ID: {}", hotelId, e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
