package com.example.cupid.utils;

import com.example.cupid.entity.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TestDataBuilder {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Property createTestProperty(Long hotelId, String name, String city, Integer stars, BigDecimal rating, Integer reviewCount) {
        Property property = new Property();
        property.setHotelId(hotelId);
        property.setCupidId(hotelId + 1000L);
        property.setName(name);
        property.setHotelType("hotel");
        property.setHotelTypeId(1);
        property.setChain("Test Chain");
        property.setChainId(1);
        property.setLatitude(40.7128);
        property.setLongitude(-74.0060);
        property.setPhone("+1-555-123-4567");
        property.setEmail("test@hotel.com");
        property.setStars(stars);
        property.setRating(rating);
        property.setReviewCount(reviewCount);
        property.setDescriptionHtml("<p>Test description</p>");
        property.setMarkdownDescription("Test description");
        property.setImportantInfo("Important information");
        property.setUpdatedAt(Instant.now());
        property.setCreatedAt(Instant.now());
        
        // Set address JSON for city-based searches
        String addressJson = String.format("{\"city\": \"%s\", \"country\": \"Test Country\", \"address\": \"123 Test Street\"}", city);
        property.setAddressJson(addressJson);
        
        // Initialize collections
        property.setPhotos(new ArrayList<>());
        property.setFacilities(new ArrayList<>());
        property.setRooms(new ArrayList<>());
        property.setPolicies(new ArrayList<>());
        property.setReviews(new ArrayList<>());
        property.setTranslations(new ArrayList<>());
        
        return property;
    }

    public static PropertyPhoto createTestPropertyPhoto(String url, String description) {
        PropertyPhoto photo = new PropertyPhoto();
        photo.setUrl(url);
        photo.setHdUrl(url.replace(".jpg", "_hd.jpg"));
        photo.setImageDescription(description);
        photo.setImageClass1("lobby");
        photo.setMainPhoto(true);
        photo.setScore(BigDecimal.valueOf(9.5));
        photo.setClassId(1);
        photo.setClassOrder(1);
        return photo;
    }

    public static PropertyFacility createTestPropertyFacility(Integer facilityId, String name) {
        PropertyFacility facility = new PropertyFacility();
        facility.setFacilityId(facilityId);
        facility.setFacilityName(name);
        return facility;
    }

    public static Room createTestRoom(Long roomId, String roomName, String description) {
        Room room = new Room();
        room.setId(roomId);
        room.setRoomName(roomName);
        room.setDescription(description);
        room.setRoomSizeSquare(BigDecimal.valueOf(25.5));
        room.setRoomSizeUnit("m²");
        room.setMaxAdults(2);
        room.setMaxChildren(1);
        room.setMaxOccupancy(3);
        room.setBedRelation("NONE");
        room.setBedTypesJson("[{\"bed_type\": \"Double bed\", \"quantity\": 1}]");
        room.setViewsJson("[]");
        room.setPhotos(new ArrayList<>());
        room.setAmenities(new ArrayList<>());
        return room;
    }

    public static RoomPhoto createTestRoomPhoto(String url, String description) {
        RoomPhoto photo = new RoomPhoto();
        photo.setUrl(url);
        photo.setHdUrl(url.replace(".jpg", "_hd.jpg"));
        photo.setImageDescription(description);
        photo.setImageClass1("bedroom");
        photo.setMainPhoto(true);
        photo.setScore(BigDecimal.valueOf(9.0));
        photo.setClassId(1);
        photo.setClassOrder(1);
        return photo;
    }

    public static RoomAmenity createTestRoomAmenity(Integer amenityId, String name) {
        RoomAmenity amenity = new RoomAmenity();
        amenity.setAmenitiesId(amenityId);
        amenity.setName(name);
        amenity.setSort(1);
        return amenity;
    }

    public static Policy createTestPolicy(String policyType, String name, String description) {
        Policy policy = new Policy();
        policy.setPolicyType(policyType);
        policy.setName(name);
        policy.setDescription(description);
        return policy;
    }

    public static Review createTestReview(BigDecimal averageScore, String country, String name, String headline) {
        Review review = new Review();
        review.setAverageScore(averageScore);
        review.setCountry(country);
        review.setType("verified");
        review.setName(name);
        review.setReviewDate(Instant.now());
        review.setHeadline(headline);
        review.setLanguage("en");
        review.setPros("Clean rooms, friendly staff");
        review.setCons("Noisy at night");
        review.setSource("booking.com");
        return review;
    }

    public static PropertyTranslation createTestTranslation(String lang, String description) {
        PropertyTranslation translation = new PropertyTranslation();
        translation.setLang(lang);
        translation.setDescriptionHtml("<p>" + description + "</p>");
        translation.setMarkdownDescription(description);
        translation.setFetchedAt(Instant.now());
        return translation;
    }

    public static JsonNode createMockPropertyJsonNode(Long hotelId, String name) {
        ObjectNode propertyNode = objectMapper.createObjectNode();
        propertyNode.put("hotel_id", hotelId);
        propertyNode.put("cupid_id", hotelId + 1000L);
        propertyNode.put("hotel_name", name);
        propertyNode.put("hotel_type", "hotel");
        propertyNode.put("hotel_type_id", 1);
        propertyNode.put("chain", "Test Chain");
        propertyNode.put("chain_id", 1);
        propertyNode.put("latitude", 40.7128);
        propertyNode.put("longitude", -74.0060);
        propertyNode.put("phone", "+1-555-123-4567");
        propertyNode.put("email", "test@hotel.com");
        propertyNode.put("stars", 4);
        propertyNode.put("rating", 8.5);
        propertyNode.put("review_count", 100);
        propertyNode.put("description", "<p>Test description</p>");
        propertyNode.put("markdown_description", "Test description");
        propertyNode.put("important_info", "Important information");

        // Add photos array
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
        propertyNode.set("photos", photosArray);

        // Add facilities array
        ArrayNode facilitiesArray = objectMapper.createArrayNode();
        ObjectNode facility = objectMapper.createObjectNode();
        facility.put("facility_id", 1);
        facility.put("name", "WiFi");
        facilitiesArray.add(facility);
        propertyNode.set("facilities", facilitiesArray);

        // Add rooms array
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
        propertyNode.set("rooms", roomsArray);

        // Add policies array
        ArrayNode policiesArray = objectMapper.createArrayNode();
        ObjectNode policy = objectMapper.createObjectNode();
        policy.put("policy_type", "cancellation");
        policy.put("name", "Free cancellation");
        policy.put("description", "Cancel up to 24 hours before check-in");
        policiesArray.add(policy);
        propertyNode.set("policies", policiesArray);

        return propertyNode;
    }

    public static JsonNode createMockTranslationJsonNode(String description) {
        ObjectNode translationNode = objectMapper.createObjectNode();
        translationNode.put("description", "<p>" + description + "</p>");
        translationNode.put("markdown_description", description);
        return translationNode;
    }

    public static JsonNode createMockReviewsJsonNode() {
        ArrayNode reviewsArray = objectMapper.createArrayNode();
        ObjectNode review = objectMapper.createObjectNode();
        review.put("average_score", 8.5);
        review.put("country", "France");
        review.put("type", "verified");
        review.put("name", "John Doe");
        review.put("date", "2024-01-15 10:30:00");
        review.put("headline", "Great stay!");
        review.put("language", "en");
        review.put("pros", "Clean rooms, friendly staff");
        review.put("cons", "Noisy at night");
        review.put("source", "booking.com");
        reviewsArray.add(review);
        return reviewsArray;
    }

    public static List<Property> createTestPropertyList() {
        List<Property> properties = new ArrayList<>();
        properties.add(createTestProperty(1270324L, "Test Hotel 1", "Paris", 4, BigDecimal.valueOf(8.5), 100));
        properties.add(createTestProperty(67890L, "Test Hotel 2", "London", 5, BigDecimal.valueOf(9.0), 200));
        properties.add(createTestProperty(11111L, "Another Hotel", "Paris", 3, BigDecimal.valueOf(7.5), 50));
        return properties;
    }
}
