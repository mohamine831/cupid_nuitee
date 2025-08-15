package com.example.cupid.api.v1.dto;

import lombok.Value;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

@Value
public class PropertyFacilityDto implements Serializable {
    Long id;
    @JsonProperty("facility_id")
    Integer facilityId;
    @JsonProperty("facility_name")
    String facilityName;
}
