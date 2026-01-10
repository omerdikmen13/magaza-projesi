package com.magazaapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alt_kategori")
public class AltKategori {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kategori_id", nullable = false)
    private Kategori kategori;

    @Column(nullable = false, length = 50)
    private String ad; // Elbise, Tişört, Pantolon, Kazak...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sezon sezon = Sezon.MEVSIMLIK;

    // Constructors
    public AltKategori() {
    }

    public AltKategori(String ad, Kategori kategori, Sezon sezon) {
        this.ad = ad;
        this.kategori = kategori;
        this.sezon = sezon;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Kategori getKategori() {
        return kategori;
    }

    public void setKategori(Kategori kategori) {
        this.kategori = kategori;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public Sezon getSezon() {
        return sezon;
    }

    public void setSezon(Sezon sezon) {
        this.sezon = sezon;
    }
}
