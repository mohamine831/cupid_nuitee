package com.example.cupid.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.io.Serializable;

import java.math.BigDecimal;

@Value
public class PropertyPhotoDto implements Serializable {
    Long id;
    String url;
    @JsonProperty("hd_url")
    String hdUrl;
    @JsonProperty("image_description")
    String imageDescription;
    @JsonProperty("image_class1")
    String imageClass1;
    @JsonProperty("main_photo")
    Boolean mainPhoto;
    BigDecimal score;
    @JsonProperty("class_id")
    Integer classId;
    @JsonProperty("class_order")
    Integer classOrder;
}
