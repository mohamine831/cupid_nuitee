package com.example.cupid.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "room")
@Data
public class Room {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Property property;

    @Column(name = "room_name")
    private String roomName;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "room_size_square")
    private BigDecimal roomSizeSquare;

    @Column(name = "room_size_unit")
    private String roomSizeUnit;

    @Column(name = "max_adults")
    private Integer maxAdults;

    @Column(name = "max_children")
    private Integer maxChildren;

    @Column(name = "max_occupancy")
    private Integer maxOccupancy;

    @Column(name = "bed_relation")
    private String bedRelation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bed_types_json", columnDefinition = "jsonb")
    private String bedTypesJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "views_json", columnDefinition = "jsonb")
    private String viewsJson;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomPhoto> photos;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomAmenity> amenities;
}
