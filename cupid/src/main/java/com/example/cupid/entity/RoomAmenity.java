package com.example.cupid.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "room_amenity")
@Data
public class RoomAmenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "amenities_id")
    private Integer amenitiesId;

    @Column(name = "name")
    private String name;

    @Column(name = "sort")
    private Integer sort;
}
