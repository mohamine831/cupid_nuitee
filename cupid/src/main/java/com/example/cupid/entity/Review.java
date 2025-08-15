package com.example.cupid.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "review")
@Data
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Property property;

    @Column(name = "average_score")
    private BigDecimal averageScore;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "type")
    private String type;

    @Column(name = "name")
    private String name;

    @Column(name = "review_date")
    private Instant reviewDate;

    @Column(name = "headline")
    private String headline;

    @Column(name = "language", length = 10)
    private String language;

    @Column(columnDefinition = "text")
    private String pros;

    @Column(columnDefinition = "text")
    private String cons;

    @Column(name = "source")
    private String source;
}