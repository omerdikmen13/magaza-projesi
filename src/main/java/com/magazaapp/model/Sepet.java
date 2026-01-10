package com.magazaapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sepet", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "kullanici_id", "urun_id", "beden_id" })
})
public class Sepet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private Kullanici kullanici;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "urun_id", nullable = false)
    private Urun urun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beden_id", nullable = false)
    private Beden beden;

    @Column(nullable = false)
    private Integer adet = 1;

    @Column(name = "ekleme_tarihi")
    private LocalDateTime eklemeTarihi = LocalDateTime.now();

    // Constructors
    public Sepet() {
    }

    public Sepet(Kullanici kullanici, Urun urun, Beden beden, Integer adet) {
        this.kullanici = kullanici;
        this.urun = urun;
        this.beden = beden;
        this.adet = adet;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Kullanici getKullanici() {
        return kullanici;
    }

    public void setKullanici(Kullanici kullanici) {
        this.kullanici = kullanici;
    }

    public Urun getUrun() {
        return urun;
    }

    public void setUrun(Urun urun) {
        this.urun = urun;
    }

    public Beden getBeden() {
        return beden;
    }

    public void setBeden(Beden beden) {
        this.beden = beden;
    }

    public Integer getAdet() {
        return adet;
    }

    public void setAdet(Integer adet) {
        this.adet = adet;
    }

    public LocalDateTime getEklemeTarihi() {
        return eklemeTarihi;
    }

    public void setEklemeTarihi(LocalDateTime eklemeTarihi) {
        this.eklemeTarihi = eklemeTarihi;
    }
}
