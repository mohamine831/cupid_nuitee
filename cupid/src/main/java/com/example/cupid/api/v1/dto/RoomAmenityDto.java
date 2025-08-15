package com.example.cupid.api.v1.dto;

import lombok.Value;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

@Value
public class RoomAmenityDto implements Serializable {
    Long id;
    String name;
    @JsonProperty("amenities_id")
    Integer amenitiesId;
    Integer sort;
}
