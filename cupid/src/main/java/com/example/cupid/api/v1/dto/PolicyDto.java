package com.example.cupid.api.v1.dto;

import lombok.Value;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

@Value
public class PolicyDto implements Serializable {
    Long id;
    String name;
    String description;
    @JsonProperty("policy_type")
    String policyType;
}
