package com.example.cupid.service;

import com.example.cupid.api.v1.dto.ReviewDto;
import com.example.cupid.entity.Review;
import com.example.cupid.repository.ReviewRepository;
import com.example.cupid.api.v1.V1Mapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final V1Mapper mapper;

    @Cacheable(value = "reviews", key = "'hotel_' + #hotelId")
    public List<ReviewDto> getReviewsByHotelId(Long hotelId) {
        log.debug("Fetching reviews for hotel: {}", hotelId);
        List<Review> reviews = reviewRepository.findByPropertyHotelId(hotelId);
        return reviews.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Cacheable(value = "reviews", key = "'hotel_' + #hotelId + '_page_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ReviewDto> getReviewsByHotelIdPaged(Long hotelId, Pageable pageable) {
        log.debug("Fetching paged reviews for hotel: {}, page: {}, size: {}", 
                hotelId, pageable.getPageNumber(), pageable.getPageSize());
        Page<Review> reviews = reviewRepository.findByPropertyHotelIdPaged(hotelId, pageable);
        return reviews.map(mapper::toDto);
    }

    @Cacheable(value = "reviews", key = "'hotel_' + #hotelId + '_top_' + #limit")
    public List<ReviewDto> getTopReviewsByHotelId(Long hotelId, int limit) {
        log.debug("Fetching top {} reviews for hotel: {}", limit, hotelId);
        List<Review> reviews = reviewRepository.findTopReviewsByHotelId(hotelId, limit);
        return reviews.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Cacheable(value = "reviews", key = "'hotel_' + #hotelId + '_recent_' + #limit")
    public List<ReviewDto> getRecentReviewsByHotelId(Long hotelId, int limit) {
        log.debug("Fetching recent {} reviews for hotel: {}", limit, hotelId);
        List<Review> reviews = reviewRepository.findRecentReviewsByHotelId(hotelId, limit);
        return reviews.stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "reviews", key = "'hotel_' + #hotelId")
    public void addReview(Long hotelId, Review review) {
        log.info("Adding review for hotel: {}", hotelId);
        // This method would add a new review and evict the cache
        // Implementation depends on your business logic
    }

    @Transactional
    @CacheEvict(value = "reviews", allEntries = true)
    public void refreshReviews() {
        log.info("Refreshing all review caches");
        // This method would refresh review data and evict all caches
    }
}

