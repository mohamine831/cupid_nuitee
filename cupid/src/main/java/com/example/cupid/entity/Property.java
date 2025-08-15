package com.example.cupid.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "property")
@Data
public class Property {
    @Id
    @Column(name = "hotel_id")
    private Long hotelId;

    @Column(name = "cupid_id")
    private Long cupidId;

    @Column(name = "name")
    private String name;

    @Column(name = "hotel_type")
    private String hotelType;

    @Column(name = "hotel_type_id")
    private Integer hotelTypeId;

    @Column(name = "chain")
    private String chain;

    @Column(name = "chain_id")
    private Integer chainId;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "fax")
    private String fax;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "address_json", columnDefinition = "jsonb")
    private String addressJson;

    @Column(name = "stars")
    private Integer stars;

    @Column(name = "rating")
    private BigDecimal rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "pets_allowed")
    private Boolean petsAllowed;

    @Column(name = "child_allowed")
    private Boolean childAllowed;

    @Column(name = "airport_code")
    private String airportCode;

    @Column(name = "group_room_min")
    private Integer groupRoomMin;

    @Column(name = "main_image_th")
    private String mainImageTh;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "checkin_json", columnDefinition = "jsonb")
    private String checkinJson;

    @Column(name = "parking")
    private String parking;

    @Column(columnDefinition = "text")
    private String descriptionHtml;

    @Column(columnDefinition = "text")
    private String markdownDescription;

    @Column(columnDefinition = "text")
    private String importantInfo;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyPhoto> photos;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyFacility> facilities;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Policy> policies;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertyTranslation> translations;
}
