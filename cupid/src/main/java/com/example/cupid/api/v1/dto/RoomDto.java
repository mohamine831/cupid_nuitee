package com.example.cupid.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Value
public class RoomDto implements Serializable {
    Long id;
    @JsonProperty("room_name")
    String roomName;
    String description;
    @JsonProperty("room_size_square")
    BigDecimal roomSizeSquare;
    @JsonProperty("room_size_unit")
    String roomSizeUnit;
    @JsonProperty("max_adults")
    Integer maxAdults;
    @JsonProperty("max_children")
    Integer maxChildren;
    @JsonProperty("max_occupancy")
    Integer maxOccupancy;
    @JsonProperty("bed_relation")
    String bedRelation;
    @JsonProperty("bed_types_json")
    String bedTypesJson;
    @JsonProperty("views_json")
    String viewsJson;
    List<RoomPhotoDto> photos;
    List<RoomAmenityDto> amenities;
}
