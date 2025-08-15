package com.example.cupid.repository;

import com.example.cupid.entity.Property;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    @Cacheable(value = "properties", key = "#hotelId")
    @Query("SELECT p FROM Property p " +
           "LEFT JOIN FETCH p.photos " +
           "WHERE p.hotelId = :hotelId")
    Optional<Property> findByIdWithAllDetails(@Param("hotelId") Long hotelId);

    @Cacheable(value = "hotels", key = "'page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Query("SELECT p FROM Property p " +
           "LEFT JOIN FETCH p.photos " +
           "ORDER BY p.rating DESC, p.reviewCount DESC")
    Page<Property> findAllWithBasicDetails(Pageable pageable);

    @Cacheable(value = "properties", key = "'hotel_' + #hotelId")
    @Query("SELECT p FROM Property p WHERE p.hotelId = :hotelId")
    Optional<Property> findByHotelId(@Param("hotelId") Long hotelId);

    @Cacheable(value = "properties", key = "'search_' + #name + '_' + #city")
    @Query("SELECT p FROM Property p " +
           "WHERE LOWER(p.name) LIKE LOWER(:name) " +
           "ORDER BY p.rating DESC")
    List<Property> searchByNameAndCity(@Param("name") String name, @Param("city") String city);

    // Native query for more efficient city search in JSONB
    @Query(value = "SELECT p.* FROM property p " +
           "WHERE LOWER(p.name) LIKE LOWER(:name) " +
           "AND p.address_json::text ILIKE :city " +
           "ORDER BY p.rating DESC", nativeQuery = true)
    List<Property> searchByNameAndCityNative(@Param("name") String name, @Param("city") String city);

    // Search by name only
    @Cacheable(value = "properties", key = "'search_name_' + #name")
    @Query("SELECT p FROM Property p " +
           "WHERE LOWER(p.name) LIKE LOWER(:name) " +
           "ORDER BY p.rating DESC")
    List<Property> searchByName(@Param("name") String name);
}