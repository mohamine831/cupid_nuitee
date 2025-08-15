package com.example.cupid.repository;

import com.example.cupid.entity.PropertyTranslation;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyTranslationRepository extends JpaRepository<PropertyTranslation, Long> {
    
    @Cacheable(value = "translations", key = "'hotel_' + #hotelId")
    @Query("SELECT t FROM PropertyTranslation t WHERE t.property.hotelId = :hotelId ORDER BY t.lang")
    List<PropertyTranslation> findByPropertyHotelId(@Param("hotelId") Long hotelId);

    @Cacheable(value = "translations", key = "'hotel_' + #hotelId + '_lang_' + #lang")
    @Query("SELECT t FROM PropertyTranslation t WHERE t.property.hotelId = :hotelId AND t.lang = :lang")
    Optional<PropertyTranslation> findByPropertyHotelIdAndLang(@Param("hotelId") Long hotelId, @Param("lang") String lang);

    @Cacheable(value = "translations", key = "'hotel_' + #hotelId + '_recent'")
    @Query("SELECT t FROM PropertyTranslation t WHERE t.property.hotelId = :hotelId ORDER BY t.fetchedAt DESC")
    List<PropertyTranslation> findRecentTranslationsByHotelId(@Param("hotelId") Long hotelId);
}