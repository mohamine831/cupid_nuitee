package com.example.cupid.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Value
public class PropertyDto implements Serializable {
    @JsonProperty("hotel_id")
    Long hotelId;

    @JsonProperty("cupid_id")
    Long cupidId;

    String name;

    @JsonProperty("hotel_type")
    String hotelType;

    @JsonProperty("hotel_type_id")
    Integer hotelTypeId;

    String chain;

    @JsonProperty("chain_id")
    Integer chainId;

    Double latitude;
    Double longitude;

    @JsonProperty("address_json")
    String addressJson;

    Integer stars;
    BigDecimal rating;

    @JsonProperty("review_count")
    Integer reviewCount;

    String phone;
    String email;
    String fax;

    @JsonProperty("pets_allowed")
    Boolean petsAllowed;

    @JsonProperty("child_allowed")
    Boolean childAllowed;

    @JsonProperty("airport_code")
    String airportCode;

    @JsonProperty("group_room_min")
    Integer groupRoomMin;

    @JsonProperty("main_image_th")
    String mainImageTh;

    @JsonProperty("checkin_json")
    String checkinJson;

    String parking;

    @JsonProperty("description_html")
    String descriptionHtml;

    @JsonProperty("markdown_description")
    String markdownDescription;

    @JsonProperty("important_info")
    String importantInfo;

    @JsonProperty("created_at")
    Instant createdAt;

    @JsonProperty("updated_at")
    Instant updatedAt;

    List<PropertyPhotoDto> photos;
    List<PropertyFacilityDto> facilities;
    List<RoomDto> rooms;
    List<PolicyDto> policies;
    List<ReviewDto> reviews;
    List<PropertyTranslationDto> translations;
}
