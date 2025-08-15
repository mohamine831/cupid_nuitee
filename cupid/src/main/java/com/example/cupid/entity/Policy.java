package com.example.cupid.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "policy")
@Data
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hotel_id")
    private Property property;

    @Column(name = "policy_type")
    private String policyType;

    @Column(name = "name")
    private String name;

    @Column(columnDefinition = "text")
    private String description;
}
