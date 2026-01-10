package com.magazaapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "urun", indexes = {
        @Index(name = "idx_magaza", columnList = "magaza_id"),
        @Index(name = "idx_kategori", columnList = "alt_kategori_id")
})
public class Urun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magaza_id", nullable = false)
    private Magaza magaza;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alt_kategori_id", nullable = false)
    private AltKategori altKategori;

    @Column(nullable = false, length = 200)
    private String ad;

    @Column(columnDefinition = "TEXT")
    private String aciklama;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fiyat;

    @Column(name = "resim_url", length = 500)
    private String resimUrl;

    @Column(length = 50)
    private String renk;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sezon sezon = Sezon.TUM_SEZON;

    @Column(nullable = false)
    private Boolean aktif = true;

    @Column(name = "olusturma_tarihi")
    private LocalDateTime olusturmaTarihi = LocalDateTime.now();

    // Constructors
    public Urun() {
    }

    public Urun(String ad, BigDecimal fiyat, Magaza magaza, AltKategori altKategori) {
        this.ad = ad;
        this.fiyat = fiyat;
        this.magaza = magaza;
        this.altKategori = altKategori;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Magaza getMagaza() {
        return magaza;
    }

    public void setMagaza(Magaza magaza) {
        this.magaza = magaza;
    }

    public AltKategori getAltKategori() {
        return altKategori;
    }

    public void setAltKategori(AltKategori altKategori) {
        this.altKategori = altKategori;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public BigDecimal getFiyat() {
        return fiyat;
    }

    public void setFiyat(BigDecimal fiyat) {
        this.fiyat = fiyat;
    }

    public String getResimUrl() {
        return resimUrl;
    }

    public void setResimUrl(String resimUrl) {
        this.resimUrl = resimUrl;
    }

    public String getRenk() {
        return renk;
    }

    public void setRenk(String renk) {
        this.renk = renk;
    }

    public Sezon getSezon() {
        return sezon;
    }

    public void setSezon(Sezon sezon) {
        this.sezon = sezon;
    }

    public Boolean getAktif() {
        return aktif;
    }

    public void setAktif(Boolean aktif) {
        this.aktif = aktif;
    }

    public LocalDateTime getOlusturmaTarihi() {
        return olusturmaTarihi;
    }

    public void setOlusturmaTarihi(LocalDateTime olusturmaTarihi) {
        this.olusturmaTarihi = olusturmaTarihi;
    }
}
