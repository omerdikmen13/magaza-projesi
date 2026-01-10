package com.magazaapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "beden")
public class Beden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String ad; // XS, S, M, L, XL, XXL

    // Constructors
    public Beden() {
    }

    public Beden(String ad) {
        this.ad = ad;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }
}
