package com.magazaapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mesaj")
public class Mesaj {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gonderen_id", nullable = false)
    private Kullanici gonderen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "musteri_id")
    private Kullanici musteri; // Sohbetin ait olduğu müşteri (mağaza sahibi mesaj gönderdiğinde de set edilir)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magaza_id", nullable = false)
    private Magaza magaza;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String icerik;

    @Column(name = "tarih", nullable = false)
    private LocalDateTime tarih = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean okundu = false;

    @Column(name = "gonderen_musteri")
    private Boolean gonderenMusteri = true; // true = müşteri gönderdi, false = mağaza sahibi gönderdi

    // Constructors
    public Mesaj() {
    }

    public Mesaj(Kullanici gonderen, Kullanici musteri, Magaza magaza, String icerik, Boolean gonderenMusteri) {
        this.gonderen = gonderen;
        this.musteri = musteri;
        this.magaza = magaza;
        this.icerik = icerik;
        this.gonderenMusteri = gonderenMusteri;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Kullanici getGonderen() {
        return gonderen;
    }

    public void setGonderen(Kullanici gonderen) {
        this.gonderen = gonderen;
    }

    public Kullanici getMusteri() {
        return musteri;
    }

    public void setMusteri(Kullanici musteri) {
        this.musteri = musteri;
    }

    public Magaza getMagaza() {
        return magaza;
    }

    public void setMagaza(Magaza magaza) {
        this.magaza = magaza;
    }

    public String getIcerik() {
        return icerik;
    }

    public void setIcerik(String icerik) {
        this.icerik = icerik;
    }

    public LocalDateTime getTarih() {
        return tarih;
    }

    public void setTarih(LocalDateTime tarih) {
        this.tarih = tarih;
    }

    public Boolean getOkundu() {
        return okundu;
    }

    public void setOkundu(Boolean okundu) {
        this.okundu = okundu;
    }

    public Boolean getGonderenMusteri() {
        return gonderenMusteri;
    }

    public void setGonderenMusteri(Boolean gonderenMusteri) {
        this.gonderenMusteri = gonderenMusteri;
    }
}
