package com.example.cupid.service;

import com.example.cupid.api.v1.dto.PropertyTranslationDto;
import com.example.cupid.entity.PropertyTranslation;
import com.example.cupid.repository.PropertyTranslationRepository;
import com.example.cupid.api.v1.V1Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TranslationService {

    private final PropertyTranslationRepository translationRepository;
    private final V1Mapper mapper;

    @Cacheable(value = "translations", key = "'hotel_' + #hotelId")
    public List<PropertyTranslationDto> getTranslationsByHotelId(Long hotelId) {
        log.debug("Fetching translations for hotel: {}", hotelId);
        List<PropertyTranslation> translations = translationRepository.findByPropertyHotelId(hotelId);
        return translations.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Cacheable(value = "translations", key = "'hotel_' + #hotelId + '_lang_' + #lang")
    public Optional<PropertyTranslationDto> getTranslationByHotelIdAndLang(Long hotelId, String lang) {
        log.debug("Fetching translation for hotel: {} and language: {}", hotelId, lang);
        return translationRepository.findByPropertyHotelIdAndLang(hotelId, lang)
                .map(mapper::toDto);
    }

    @Cacheable(value = "translations", key = "'hotel_' + #hotelId + '_recent'")
    public List<PropertyTranslationDto> getRecentTranslationsByHotelId(Long hotelId) {
        log.debug("Fetching recent translations for hotel: {}", hotelId);
        List<PropertyTranslation> translations = translationRepository.findRecentTranslationsByHotelId(hotelId);
        return translations.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "translations", key = "'hotel_' + #hotelId")
    public void addTranslation(Long hotelId, PropertyTranslation translation) {
        log.info("Adding translation for hotel: {}", hotelId);
        // This method would add a new translation and evict the cache
        // Implementation depends on your business logic
    }

    @Transactional
    @CacheEvict(value = "translations", allEntries = true)
    public void refreshTranslations() {
        log.info("Refreshing all translation caches");
        // This method would refresh translation data and evict all caches
    }
}

