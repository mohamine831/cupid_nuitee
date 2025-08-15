package com.example.cupid.service;

import com.example.cupid.client.CupidApiClient;
import com.example.cupid.entity.*;
import com.example.cupid.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class CupidFetchService {
    private final CupidApiClient apiClient;
    private final PropertyRepository propertyRepository;
    private final PropertyTranslationRepository translationRepository;
    private final ReviewRepository reviewRepository;
    private final PropertyPhotoRepository propertyPhotoRepository;
    private final RoomPhotoRepository roomPhotoRepository;
    private final PropertyFacilityRepository propertyFacilityRepository;
    private final RoomRepository roomRepository;
    private final RoomAmenityRepository roomAmenityRepository;
    private final PolicyRepository policyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void fetchAndSave(long hotelId, int reviewsToFetch) {
        log.info("Starting to fetch and save hotel with ID: {}", hotelId);

        try {
            Mono<JsonNode> propMono = apiClient.fetchProperty(hotelId);
            log.info("Created Mono for hotel ID: {}", hotelId);

            JsonNode propResp = propMono.block();
            log.info("API response for hotel ID {}: {}", hotelId, propResp);

            if (propResp == null) {
                log.error("API response is null for hotel ID: {}", hotelId);
                return;
            }


            JsonNode data = propResp;
            log.info("Property data for hotel ID {}: {}", hotelId, data);

            Property p = mapProperty(data);

            // translations
            if (p.getTranslations() != null) {
                p.getTranslations().clear();
            } else {
                p.setTranslations(new ArrayList<>());
            }
            for (String lang : new String[]{"fr", "es"}) {
                try {
                    JsonNode tnode = apiClient.fetchTranslation(hotelId, lang).block();
                    if (tnode != null && !tnode.isNull()) {
                        // Check if translation already exists for this hotel and language
                        Optional<PropertyTranslation> existingTranslation = translationRepository.findByPropertyHotelIdAndLang(hotelId, lang);
                        
                        PropertyTranslation tr;
                        if (existingTranslation.isPresent()) {
                            // Update existing translation
                            tr = existingTranslation.get();
                            tr.setDescriptionHtml(tnode.has("description") ? tnode.get("description").asText() : null);
                            tr.setMarkdownDescription(tnode.has("markdown_description") ? tnode.get("markdown_description").asText() : null);
                            tr.setFetchedAt(Instant.now());
                        } else {
                            // Create new translation
                            tr = new PropertyTranslation();
                            tr.setProperty(p);
                            tr.setLang(lang);
                            tr.setDescriptionHtml(tnode.has("description") ? tnode.get("description").asText() : null);
                            tr.setMarkdownDescription(tnode.has("markdown_description") ? tnode.get("markdown_description").asText() : null);
                        }
                        p.getTranslations().add(tr);
                    } else {
                        log.debug("No translation data found for hotel ID: {} and language: {}", hotelId, lang);
                    }
                } catch (Exception e) {
                    log.error("Error fetching translation for hotel ID: {} and language: {}", hotelId, lang, e);
                }
            }

            // reviews
            if (p.getReviews() != null) {
                p.getReviews().clear();
            } else {
                p.setReviews(new ArrayList<>());
            }
            try {
                JsonNode rnode = apiClient.fetchReviews(hotelId, reviewsToFetch).block();
                if (rnode != null && rnode.isArray()) {
                    for (JsonNode rn : rnode) {
                        Review review = new Review();
                        review.setProperty(p);
                        if (rn.has("average_score")) review.setAverageScore(rn.get("average_score").decimalValue());
                        review.setCountry(rn.has("country") ? rn.get("country").asText() : null);
                        review.setType(rn.has("type") ? rn.get("type").asText() : null);
                        review.setName(rn.has("name") ? rn.get("name").asText() : null);
                        if (rn.has("date")) {
                            String ds = rn.get("date").asText();
                            try {
                                LocalDateTime ldt = LocalDateTime.parse(ds.replace(" ", "T"));
                                review.setReviewDate(ldt.toInstant(ZoneOffset.UTC));
                            } catch (Exception e) {
                                review.setReviewDate(Instant.now());
                            }
                        }
                        review.setHeadline(rn.has("headline") ? rn.get("headline").asText() : null);
                        review.setLanguage(rn.has("language") ? rn.get("language").asText() : null);
                        review.setPros(rn.has("pros") ? rn.get("pros").asText() : null);
                        review.setCons(rn.has("cons") ? rn.get("cons").asText() : null);
                        review.setSource(rn.has("source") ? rn.get("source").asText() : null);
                        p.getReviews().add(review);
                    }
                    log.info("Saved {} reviews for hotel ID: {}", p.getReviews().size(), hotelId);
                } else {
                    log.debug("No review data found for hotel ID: {}", hotelId);
                }
            } catch (Exception e) {
                log.error("Error fetching reviews for hotel ID: {}", hotelId, e);
            }

            try {
                propertyRepository.save(p);
                log.info("Successfully completed fetch and save for hotel ID: {}", hotelId);
            } catch (Exception e) {
                log.error("Error saving property for hotel ID: {}. Error: {}", hotelId, e.getMessage());
                log.error("Property data: hotelId={}, name={}, importantInfo length={}", 
                    p.getHotelId(), p.getName(), 
                    p.getImportantInfo() != null ? p.getImportantInfo().length() : 0);
                throw e;
            }

        } catch (Exception e) {
            log.error("Error in fetchAndSave for hotel ID: {}", hotelId, e);
            throw e; // Re-throw to trigger transaction rollback
        }
    }

    private Property mapProperty(JsonNode data) {
        Property p = propertyRepository.findById(data.get("hotel_id").asLong()).orElse(new Property());

        if (p.getPhotos() != null) {
            p.getPhotos().clear();
        } else {
            p.setPhotos(new ArrayList<>());
        }
        if (data.has("photos") && data.get("photos").isArray()) {
            for (JsonNode photoNode : data.get("photos")) {
                PropertyPhoto photo = new PropertyPhoto();
                photo.setProperty(p);
                photo.setUrl(photoNode.has("url") ? photoNode.get("url").asText() : null);
                photo.setHdUrl(photoNode.has("hd_url") ? photoNode.get("hd_url").asText() : null);
                photo.setImageDescription(photoNode.has("image_description") ? photoNode.get("image_description").asText() : null);
                photo.setImageClass1(photoNode.has("image_class1") ? photoNode.get("image_class1").asText() : null);
                photo.setMainPhoto(photoNode.has("main_photo") ? photoNode.get("main_photo").asBoolean() : null);
                photo.setScore(photoNode.has("score") ? BigDecimal.valueOf(photoNode.get("score").asLong())  : null);
                photo.setClassId(photoNode.has("class_id") ? photoNode.get("class_id").asInt() : null);
                photo.setClassOrder(photoNode.has("class_order") ? photoNode.get("class_order").asInt() : null);
                p.getPhotos().add(photo);
            }
        }

        if (p.getFacilities() != null) {
            p.getFacilities().clear();
        } else {
            p.setFacilities(new ArrayList<>());
        }
        if (data.has("facilities") && data.get("facilities").isArray()) {
            for (JsonNode facilityNode : data.get("facilities")) {
                PropertyFacility facility = new PropertyFacility();
                facility.setProperty(p);
                facility.setFacilityId(facilityNode.has("facility_id") ? facilityNode.get("facility_id").asInt() : null);
                facility.setFacilityName(facilityNode.has("name") ? facilityNode.get("name").asText() : null);
                p.getFacilities().add(facility);
            }
        }

        if (p.getRooms() != null) {
            p.getRooms().clear();
        } else {
            p.setRooms(new ArrayList<>());
        }
        if (data.has("rooms") && data.get("rooms").isArray()) {
            for (JsonNode roomNode : data.get("rooms")) {
                Room room = new Room();
                room.setProperty(p);
                room.setId(roomNode.has("id") ? roomNode.get("id").asLong() : null);
                room.setRoomName(roomNode.has("room_name") ? roomNode.get("room_name").asText() : null);
                room.setDescription(roomNode.has("description") ? roomNode.get("description").asText() : null);
                room.setRoomSizeSquare(roomNode.has("room_size_square") ? roomNode.get("room_size_square").decimalValue() : null);
                room.setRoomSizeUnit(roomNode.has("room_size_unit") ? roomNode.get("room_size_unit").asText() : null);
                room.setMaxAdults(roomNode.has("max_adults") ? roomNode.get("max_adults").asInt() : null);
                room.setMaxChildren(roomNode.has("max_children") ? roomNode.get("max_children").asInt() : null);
                room.setMaxOccupancy(roomNode.has("max_occupancy") ? roomNode.get("max_occupancy").asInt() : null);
                room.setBedRelation(roomNode.has("bed_relation") ? roomNode.get("bed_relation").asText() : null);
                
                // Extract bed_types JSON with proper validation
                if (roomNode.has("bed_types") && !roomNode.get("bed_types").isNull()) {
                    try {
                        String bedTypesJson = objectMapper.writeValueAsString(roomNode.get("bed_types"));
                        if (bedTypesJson != null && !bedTypesJson.trim().isEmpty()) {
                            room.setBedTypesJson(bedTypesJson);
                        } else {
                            room.setBedTypesJson(null);
                        }
                    } catch (Exception e) {
                        log.warn("Error processing bed_types JSON for room ID: {}. Setting to null.", roomNode.has("id") ? roomNode.get("id").asLong() : "unknown");
                        room.setBedTypesJson(null);
                    }
                } else {
                    room.setBedTypesJson(null);
                }
                
                // Extract views JSON with proper validation
                if (roomNode.has("views") && !roomNode.get("views").isNull()) {
                    try {
                        String viewsJson = objectMapper.writeValueAsString(roomNode.get("views"));
                        if (viewsJson != null && !viewsJson.trim().isEmpty()) {
                            room.setViewsJson(viewsJson);
                        } else {
                            room.setViewsJson(null);
                        }
                    } catch (Exception e) {
                        log.warn("Error processing views JSON for room ID: {}. Setting to null.", roomNode.has("id") ? roomNode.get("id").asLong() : "unknown");
                        room.setViewsJson(null);
                    }
                } else {
                    room.setViewsJson(null);
                }

                if (room.getAmenities() != null) {
                    room.getAmenities().clear();
                } else {
                    room.setAmenities(new ArrayList<>());
                }
                if (roomNode.has("room_amenities") && roomNode.get("room_amenities").isArray()) {
                    for (JsonNode amenityNode : roomNode.get("room_amenities")) {
                        RoomAmenity amenity = new RoomAmenity();
                        amenity.setRoom(room);
                        amenity.setAmenitiesId(amenityNode.has("amenities_id") ? amenityNode.get("amenities_id").asInt() : null);
                        amenity.setName(amenityNode.has("name") ? amenityNode.get("name").asText() : null);
                        amenity.setSort(amenityNode.has("sort") ? amenityNode.get("sort").asInt() : null);
                        room.getAmenities().add(amenity);
                    }
                }

                if (room.getPhotos() != null) {
                    room.getPhotos().clear();
                } else {
                    room.setPhotos(new ArrayList<>());
                }
                if (roomNode.has("photos") && roomNode.get("photos").isArray()) {
                    for (JsonNode photoNode : roomNode.get("photos")) {
                        RoomPhoto photo = new RoomPhoto();
                        photo.setRoom(room);
                        photo.setUrl(photoNode.has("url") ? photoNode.get("url").asText() : null);
                        photo.setHdUrl(photoNode.has("hd_url") ? photoNode.get("hd_url").asText() : null);
                        photo.setImageDescription(photoNode.has("image_description") ? photoNode.get("image_description").asText() : null);
                        photo.setImageClass1(photoNode.has("image_class1") ? photoNode.get("image_class1").asText() : null);
                        photo.setMainPhoto(photoNode.has("main_photo") ? photoNode.get("main_photo").asBoolean() : null);
                        photo.setScore(photoNode.has("score") ? photoNode.get("score").decimalValue()  : null);
                        photo.setClassId(photoNode.has("class_id") ? photoNode.get("class_id").asInt() : null);
                        photo.setClassOrder(photoNode.has("class_order") ? photoNode.get("class_order").asInt() : null);
                        room.getPhotos().add(photo);
                    }
                }
                p.getRooms().add(room);
            }
        }

        if (p.getPolicies() != null) {
            p.getPolicies().clear();
        } else {
            p.setPolicies(new ArrayList<>());
        }
        if (data.has("policies") && data.get("policies").isArray()) {
            for (JsonNode policyNode : data.get("policies")) {
                Policy policy = new Policy();
                policy.setProperty(p);
                policy.setPolicyType(policyNode.has("policy_type") ? policyNode.get("policy_type").asText() : null);
                policy.setName(policyNode.has("name") ? policyNode.get("name").asText() : null);
                policy.setDescription(policyNode.has("description") ? policyNode.get("description").asText() : null);
                p.getPolicies().add(policy);
            }
        }

        p.setHotelId(data.has("hotel_id") && !data.get("hotel_id").isNull() ? data.get("hotel_id").asLong() : null);
        p.setCupidId(data.has("cupid_id") && !data.get("cupid_id").isNull() ? data.get("cupid_id").asLong() : null);
        p.setName(data.has("hotel_name") && !data.get("hotel_name").isNull() && !data.get("hotel_name").asText().trim().isEmpty() ? data.get("hotel_name").asText() : null);
        p.setHotelType(data.has("hotel_type") && !data.get("hotel_type").isNull() && !data.get("hotel_type").asText().trim().isEmpty() ? data.get("hotel_type").asText() : null);
        p.setHotelTypeId(data.has("hotel_type_id") && !data.get("hotel_type_id").isNull() ? data.get("hotel_type_id").asInt() : null);
        p.setChain(data.has("chain") && !data.get("chain").isNull() && !data.get("chain").asText().trim().isEmpty() ? data.get("chain").asText() : null);
        p.setChainId(data.has("chain_id") && !data.get("chain_id").isNull() ? data.get("chain_id").asInt() : null);
        p.setLatitude(data.has("latitude") && !data.get("latitude").isNull() ? data.get("latitude").asDouble() : null);
        p.setLongitude(data.has("longitude") && !data.get("longitude").isNull() ? data.get("longitude").asDouble() : null);
        // Handle address_json with proper null checking and JSON validation
        ObjectMapper mapper = new ObjectMapper();

        if (data.has("address") && !data.get("address").isNull()) {
            try {
                JsonNode addressNode = data.get("address");
                if (addressNode.isObject()) {
                    // Convert the nested address object to a JSON string
                    String addressJson = mapper.writeValueAsString(addressNode);
                    p.setAddressJson(addressJson);
                } else {
                    p.setAddressJson(null);
                }
            } catch (Exception e) {
                log.warn("Error processing address JSON for hotel ID: {}. Setting to null.",
                        data.has("hotel_id") ? data.get("hotel_id").asLong() : null, e);
                p.setAddressJson(null);
            }
        } else {
            p.setAddressJson(null);
        }
        // Handle text fields with proper null and empty string checking
        p.setPhone(data.has("phone") && !data.get("phone").isNull() && !data.get("phone").asText().trim().isEmpty() ? data.get("phone").asText() : null);
        p.setEmail(data.has("email") && !data.get("email").isNull() && !data.get("email").asText().trim().isEmpty() ? data.get("email").asText() : null);
        p.setFax(data.has("fax") && !data.get("fax").isNull() && !data.get("fax").asText().trim().isEmpty() ? data.get("fax").asText() : null);
        p.setStars(data.has("stars") && !data.get("stars").isNull() ? data.get("stars").asInt() : null);
        p.setRating(data.has("rating") && !data.get("rating").isNull() ? data.get("rating").decimalValue() : null);
        p.setReviewCount(data.has("review_count") && !data.get("review_count").isNull() ? data.get("review_count").asInt() : null);
        p.setPetsAllowed(data.has("pets_allowed") && !data.get("pets_allowed").isNull() ? data.get("pets_allowed").asBoolean() : null);
        p.setChildAllowed(data.has("child_allowed") && !data.get("child_allowed").isNull() ? data.get("child_allowed").asBoolean() : null);
        p.setAirportCode(data.has("airport_code") && !data.get("airport_code").isNull() && !data.get("airport_code").asText().trim().isEmpty() ? data.get("airport_code").asText() : null);
        p.setGroupRoomMin(data.has("group_room_min") && !data.get("group_room_min").isNull() ? data.get("group_room_min").asInt() : null);
        p.setMainImageTh(data.has("main_image_th") && !data.get("main_image_th").isNull() && !data.get("main_image_th").asText().trim().isEmpty() ? data.get("main_image_th").asText() : null);
        p.setParking(data.has("parking") && !data.get("parking").isNull() && !data.get("parking").asText().trim().isEmpty() ? data.get("parking").asText() : null);
        // Handle description_html with proper null checking and length validation
        if (data.has("description") && !data.get("description").isNull()) {
            String descriptionHtml = data.get("description").asText();
            if (descriptionHtml != null && !descriptionHtml.trim().isEmpty()) {
                if (descriptionHtml.length() > 10000) {
                    descriptionHtml = descriptionHtml.substring(0, 10000) + "...";
                }
                p.setDescriptionHtml(descriptionHtml);
            } else {
                p.setDescriptionHtml(null);
            }
        } else {
            p.setDescriptionHtml(null);
        }
        
        // Handle markdown_description with proper null checking and length validation
        if (data.has("markdown_description") && !data.get("markdown_description").isNull()) {
            String markdownDescription = data.get("markdown_description").asText();
            if (markdownDescription != null && !markdownDescription.trim().isEmpty()) {
                if (markdownDescription.length() > 10000) {
                    markdownDescription = markdownDescription.substring(0, 10000) + "...";
                }
                p.setMarkdownDescription(markdownDescription);
            } else {
                p.setMarkdownDescription(null);
            }
        } else {
            p.setMarkdownDescription(null);
        }
        // Handle important_info with proper null checking and length validation
        if (data.has("important_info") && !data.get("important_info").isNull()) {
            String importantInfo = data.get("important_info").asText();
            if (importantInfo != null && !importantInfo.trim().isEmpty()) {
                // Limit the length to prevent database issues (PostgreSQL TEXT can handle up to 1GB, but let's be safe)
                if (importantInfo.length() > 10000) {
                    importantInfo = importantInfo.substring(0, 10000) + "...";
                }
                p.setImportantInfo(importantInfo);
            } else {
                p.setImportantInfo(null);
            }
        } else {
            p.setImportantInfo(null);
        }
        
        // Extract checkin JSON with proper validation
        if (data.has("checkin") && !data.get("checkin").isNull()) {
            try {
                String checkinJson = objectMapper.writeValueAsString(data.get("checkin"));
                if (checkinJson != null && !checkinJson.trim().isEmpty()) {
                    p.setCheckinJson(checkinJson);
                } else {
                    p.setCheckinJson(null);
                }
            } catch (Exception e) {
                log.warn("Error processing checkin JSON for hotel ID: {}. Setting to null.", data.get("hotel_id").asLong());
                p.setCheckinJson(null);
            }
        } else {
            p.setCheckinJson(null);
        }
        
        p.setUpdatedAt(Instant.now());
        return p;
    }
}
