package com.example.cupid.repository;

import com.example.cupid.entity.Review;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    @Cacheable(value = "reviews", key = "'hotel_' + #hotelId")
    @Query("SELECT r FROM Review r WHERE r.property.hotelId = :hotelId ORDER BY r.reviewDate DESC")
    List<Review> findByPropertyHotelId(@Param("hotelId") Long hotelId);

    @Cacheable(value = "reviews", key = "'hotel_' + #hotelId + '_page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    @Query("SELECT r FROM Review r WHERE r.property.hotelId = :hotelId ORDER BY r.reviewDate DESC")
    Page<Review> findByPropertyHotelIdPaged(@Param("hotelId") Long hotelId, Pageable pageable);

    @Cacheable(value = "reviews", key = "'hotel_' + #hotelId + '_top_' + #limit")
    @Query("SELECT r FROM Review r WHERE r.property.hotelId = :hotelId ORDER BY r.averageScore DESC, r.reviewDate DESC")
    List<Review> findTopReviewsByHotelId(@Param("hotelId") Long hotelId, @Param("limit") int limit);

    @Cacheable(value = "reviews", key = "'hotel_' + #hotelId + '_recent_' + #limit")
    @Query("SELECT r FROM Review r WHERE r.property.hotelId = :hotelId ORDER BY r.reviewDate DESC")
    List<Review> findRecentReviewsByHotelId(@Param("hotelId") Long hotelId, @Param("limit") int limit);
}