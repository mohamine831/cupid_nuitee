package com.example.cupid.service;

import com.example.cupid.client.CupidApiClient;
import com.example.cupid.entity.*;
import com.example.cupid.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CupidFetchServiceTest {

    @Mock
    private CupidApiClient apiClient;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private PropertyTranslationRepository translationRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PropertyPhotoRepository propertyPhotoRepository;

    @Mock
    private RoomPhotoRepository roomPhotoRepository;

    @Mock
    private PropertyFacilityRepository propertyFacilityRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomAmenityRepository roomAmenityRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private CupidFetchService cupidFetchService;

    private ObjectMapper objectMapper;
    private ObjectNode mockPropertyData;
    private ObjectNode mockTranslationData;
    private ArrayNode mockReviewsData;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        setupMockData();
    }

    private void setupMockData() {
        // Setup mock property data
        mockPropertyData = objectMapper.createObjectNode();
        mockPropertyData.put("hotel_id", 1270324L);
        mockPropertyData.put("cupid_id", 67890L);
        mockPropertyData.put("hotel_name", "Test Hotel");
        mockPropertyData.put("hotel_type", "hotel");
        mockPropertyData.put("hotel_type_id", 1);
        mockPropertyData.put("chain", "Test Chain");
        mockPropertyData.put("chain_id", 1);
        mockPropertyData.put("latitude", 40.7128);
        mockPropertyData.put("longitude", -74.0060);
        mockPropertyData.put("phone", "+1-555-123-4567");
        mockPropertyData.put("email", "test@hotel.com");
        mockPropertyData.put("stars", 4);
        mockPropertyData.put("rating", 8.5);
        mockPropertyData.put("review_count", 100);
        mockPropertyData.put("description", "<p>Test description</p>");
        mockPropertyData.put("markdown_description", "Test description");
        mockPropertyData.put("important_info", "Important information");

        // Setup mock translation data
        mockTranslationData = objectMapper.createObjectNode();
        mockTranslationData.put("description", "<p>Description en français</p>");
        mockTranslationData.put("markdown_description", "Description en français");

        // Setup mock reviews data
        mockReviewsData = objectMapper.createArrayNode();
        ObjectNode review1 = objectMapper.createObjectNode();
        review1.put("average_score", 8.5);
        review1.put("country", "France");
        review1.put("type", "verified");
        review1.put("name", "John Doe");
        review1.put("date", "2024-01-15 10:30:00");
        review1.put("headline", "Great stay!");
        review1.put("language", "en");
        review1.put("pros", "Clean rooms, friendly staff");
        review1.put("cons", "Noisy at night");
        review1.put("source", "booking.com");
        mockReviewsData.add(review1);
    }

    @Test
    void fetchAndSave_Success() {
        // Given
        long hotelId = 1270324L;
        int reviewsToFetch = 10;

        when(propertyRepository.findById(hotelId)).thenReturn(Optional.empty());
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(apiClient.fetchProperty(hotelId)).thenReturn(Mono.just(mockPropertyData));
        when(apiClient.fetchTranslation(hotelId, "fr")).thenReturn(Mono.just(mockTranslationData));
        when(apiClient.fetchTranslation(hotelId, "es")).thenReturn(Mono.empty());
        when(apiClient.fetchReviews(hotelId, reviewsToFetch)).thenReturn(Mono.just(mockReviewsData));

        // When
        cupidFetchService.fetchAndSave(hotelId, reviewsToFetch);

        // Then
        verify(apiClient).fetchProperty(hotelId);
        verify(apiClient).fetchTranslation(hotelId, "fr");
        verify(apiClient).fetchTranslation(hotelId, "es");
        verify(apiClient).fetchReviews(hotelId, reviewsToFetch);
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void fetchAndSave_WithExistingProperty() {
        // Given
        long hotelId = 1270324L;
        int reviewsToFetch = 10;
        Property existingProperty = new Property();
        existingProperty.setHotelId(hotelId);
        existingProperty.setName("Existing Hotel");

        when(propertyRepository.findById(hotelId)).thenReturn(Optional.of(existingProperty));
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(apiClient.fetchProperty(hotelId)).thenReturn(Mono.just(mockPropertyData));
        when(apiClient.fetchTranslation(hotelId, "fr")).thenReturn(Mono.just(mockTranslationData));
        when(apiClient.fetchTranslation(hotelId, "es")).thenReturn(Mono.empty());
        when(apiClient.fetchReviews(hotelId, reviewsToFetch)).thenReturn(Mono.just(mockReviewsData));

        // When
        cupidFetchService.fetchAndSave(hotelId, reviewsToFetch);

        // Then
        verify(propertyRepository).findById(hotelId);
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void fetchAndSave_ApiResponseNull() {
        // Given
        long hotelId = 1270324L;
        int reviewsToFetch = 10;

        when(apiClient.fetchProperty(hotelId)).thenReturn(Mono.empty());

        // When
        cupidFetchService.fetchAndSave(hotelId, reviewsToFetch);

        // Then
        verify(apiClient).fetchProperty(hotelId);
        verify(propertyRepository, never()).save(any(Property.class));
    }

    @Test
    void fetchAndSave_TranslationError() {
        // Given
        long hotelId = 1270324L;
        int reviewsToFetch = 10;

        when(propertyRepository.findById(hotelId)).thenReturn(Optional.empty());
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(apiClient.fetchProperty(hotelId)).thenReturn(Mono.just(mockPropertyData));
        when(apiClient.fetchTranslation(hotelId, "fr")).thenReturn(Mono.error(new RuntimeException("API Error")));
        when(apiClient.fetchTranslation(hotelId, "es")).thenReturn(Mono.empty());
        when(apiClient.fetchReviews(hotelId, reviewsToFetch)).thenReturn(Mono.just(mockReviewsData));

        // When
        cupidFetchService.fetchAndSave(hotelId, reviewsToFetch);

        // Then
        verify(apiClient).fetchProperty(hotelId);
        verify(apiClient).fetchTranslation(hotelId, "fr");
        verify(apiClient).fetchTranslation(hotelId, "es");
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void fetchAndSave_ReviewsError() {
        // Given
        long hotelId = 1270324L;
        int reviewsToFetch = 10;

        when(propertyRepository.findById(hotelId)).thenReturn(Optional.empty());
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(apiClient.fetchProperty(hotelId)).thenReturn(Mono.just(mockPropertyData));
        when(apiClient.fetchTranslation(hotelId, "fr")).thenReturn(Mono.just(mockTranslationData));
        when(apiClient.fetchTranslation(hotelId, "es")).thenReturn(Mono.empty());
        when(apiClient.fetchReviews(hotelId, reviewsToFetch)).thenReturn(Mono.error(new RuntimeException("Reviews API Error")));

        // When
        cupidFetchService.fetchAndSave(hotelId, reviewsToFetch);

        // Then
        verify(apiClient).fetchProperty(hotelId);
        verify(apiClient).fetchReviews(hotelId, reviewsToFetch);
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void fetchAndSave_WithPhotos() {
        // Given
        long hotelId = 1270324L;
        int reviewsToFetch = 10;

        ArrayNode photosArray = objectMapper.createArrayNode();
        ObjectNode photo = objectMapper.createObjectNode();
        photo.put("url", "https://example.com/photo.jpg");
        photo.put("hd_url", "https://example.com/photo_hd.jpg");
        photo.put("image_description", "Hotel lobby");
        photo.put("image_class1", "lobby");
        photo.put("main_photo", true);
        photo.put("score", 9.5);
        photo.put("class_id", 1);
        photo.put("class_order", 1);
        photosArray.add(photo);
        mockPropertyData.set("photos", photosArray);

        when(propertyRepository.findById(hotelId)).thenReturn(Optional.empty());
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(apiClient.fetchProperty(hotelId)).thenReturn(Mono.just(mockPropertyData));
        when(apiClient.fetchTranslation(hotelId, "fr")).thenReturn(Mono.just(mockTranslationData));
        when(apiClient.fetchTranslation(hotelId, "es")).thenReturn(Mono.empty());
        when(apiClient.fetchReviews(hotelId, reviewsToFetch)).thenReturn(Mono.just(mockReviewsData));

        // When
        cupidFetchService.fetchAndSave(hotelId, reviewsToFetch);

        // Then
        verify(propertyRepository).save(argThat(property -> 
            property.getPhotos() != null && !property.getPhotos().isEmpty() &&
            property.getPhotos().get(0).getUrl().equals("https://example.com/photo.jpg")
        ));
    }

    @Test
    void fetchAndSave_WithFacilities() {
        // Given
        long hotelId = 1270324L;
        int reviewsToFetch = 10;

        ArrayNode facilitiesArray = objectMapper.createArrayNode();
        ObjectNode facility = objectMapper.createObjectNode();
        facility.put("facility_id", 1);
        facility.put("name", "WiFi");
        facilitiesArray.add(facility);
        mockPropertyData.set("facilities", facilitiesArray);

        when(propertyRepository.findById(hotelId)).thenReturn(Optional.empty());
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(apiClient.fetchProperty(hotelId)).thenReturn(Mono.just(mockPropertyData));
        when(apiClient.fetchTranslation(hotelId, "fr")).thenReturn(Mono.just(mockTranslationData));
        when(apiClient.fetchTranslation(hotelId, "es")).thenReturn(Mono.empty());
        when(apiClient.fetchReviews(hotelId, reviewsToFetch)).thenReturn(Mono.just(mockReviewsData));

        // When
        cupidFetchService.fetchAndSave(hotelId, reviewsToFetch);

        // Then
        verify(propertyRepository).save(argThat(property -> 
            property.getFacilities() != null && !property.getFacilities().isEmpty() &&
            property.getFacilities().get(0).getFacilityName().equals("WiFi")
        ));
    }

    @Test
    void fetchAndSave_WithRooms() {
        // Given
        long hotelId = 1270324L;
        int reviewsToFetch = 10;

        ArrayNode roomsArray = objectMapper.createArrayNode();
        ObjectNode room = objectMapper.createObjectNode();
        room.put("id", 1L);
        room.put("room_name", "Deluxe Room");
        room.put("description", "Spacious room with city view");
        room.put("room_size_square", 25.5);
        room.put("room_size_unit", "m²");
        room.put("max_adults", 2);
        room.put("max_children", 1);
        room.put("max_occupancy", 3);
        roomsArray.add(room);
        mockPropertyData.set("rooms", roomsArray);

        when(propertyRepository.findById(hotelId)).thenReturn(Optional.empty());
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(apiClient.fetchProperty(hotelId)).thenReturn(Mono.just(mockPropertyData));
        when(apiClient.fetchTranslation(hotelId, "fr")).thenReturn(Mono.just(mockTranslationData));
        when(apiClient.fetchTranslation(hotelId, "es")).thenReturn(Mono.empty());
        when(apiClient.fetchReviews(hotelId, reviewsToFetch)).thenReturn(Mono.just(mockReviewsData));

        // When
        cupidFetchService.fetchAndSave(hotelId, reviewsToFetch);

        // Then
        verify(propertyRepository).save(argThat(property -> 
            property.getRooms() != null && !property.getRooms().isEmpty() &&
            property.getRooms().get(0).getRoomName().equals("Deluxe Room")
        ));
    }

    @Test
    void fetchAndSave_WithPolicies() {
        // Given
        long hotelId = 1270324L;
        int reviewsToFetch = 10;

        ArrayNode policiesArray = objectMapper.createArrayNode();
        ObjectNode policy = objectMapper.createObjectNode();
        policy.put("policy_type", "cancellation");
        policy.put("name", "Free cancellation");
        policy.put("description", "Cancel up to 24 hours before check-in");
        policiesArray.add(policy);
        mockPropertyData.set("policies", policiesArray);

        when(propertyRepository.findById(hotelId)).thenReturn(Optional.empty());
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(apiClient.fetchProperty(hotelId)).thenReturn(Mono.just(mockPropertyData));
        when(apiClient.fetchTranslation(hotelId, "fr")).thenReturn(Mono.just(mockTranslationData));
        when(apiClient.fetchTranslation(hotelId, "es")).thenReturn(Mono.empty());
        when(apiClient.fetchReviews(hotelId, reviewsToFetch)).thenReturn(Mono.just(mockReviewsData));

        // When
        cupidFetchService.fetchAndSave(hotelId, reviewsToFetch);

        // Then
        verify(propertyRepository).save(argThat(property -> 
            property.getPolicies() != null && !property.getPolicies().isEmpty() &&
            property.getPolicies().get(0).getPolicyType().equals("cancellation")
        ));
    }

    @Test
    void fetchAndSave_PropertyApiError() {
        // Given
        long hotelId = 1270324L;
        int reviewsToFetch = 10;

        when(apiClient.fetchProperty(hotelId)).thenReturn(Mono.error(new RuntimeException("Property API Error")));

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            cupidFetchService.fetchAndSave(hotelId, reviewsToFetch);
        });

        verify(apiClient).fetchProperty(hotelId);
        verify(propertyRepository, never()).save(any(Property.class));
    }
}
