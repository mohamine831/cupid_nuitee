package com.example.cupid.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "property_facility")
@Data
public class PropertyFacility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Property property;

    @Column(name = "facility_id")
    private Integer facilityId;

    @Column(name = "facility_name")
    private String facilityName;
}
