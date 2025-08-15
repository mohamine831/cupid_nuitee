package com.example.cupid.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "room_photo")
@Data
public class RoomPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private String url;

    @Column(name = "hd_url")
    private String hdUrl;

    @Column(name = "image_description")
    private String imageDescription;

    @Column(name = "image_class1")
    private String imageClass1;

    @Column(name = "main_photo")
    private Boolean mainPhoto;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "class_id")
    private Integer classId;

    @Column(name = "class_order")
    private Integer classOrder;
}
