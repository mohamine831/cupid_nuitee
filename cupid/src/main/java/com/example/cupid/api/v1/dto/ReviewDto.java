package com.example.cupid.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Value
public class ReviewDto implements Serializable {
    Long id;
    @JsonProperty("average_score")
    BigDecimal averageScore;
    String country;
    String type;
    String name;
    @JsonProperty("review_date")
    Instant reviewDate;
    String headline;
    String language;
    String pros;
    String cons;
    String source;
}
