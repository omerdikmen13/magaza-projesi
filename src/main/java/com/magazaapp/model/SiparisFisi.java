package com.magazaapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "siparis_fisi", indexes = {
        @Index(name = "idx_tarih", columnList = "siparis_tarihi"),
        @Index(name = "idx_magaza_tarih", columnList = "magaza_id, siparis_tarihi")
})
public class SiparisFisi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private Kullanici kullanici;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magaza_id", nullable = false)
    private Magaza magaza;

    @Column(name = "toplam_tutar", nullable = false, precision = 10, scale = 2)
    private BigDecimal toplamTutar;

    @Column(name = "teslimat_adresi", nullable = false, columnDefinition = "TEXT")
    private String teslimatAdresi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SiparisDurum durum = SiparisDurum.BEKLEMEDE;

    @Column(name = "siparis_tarihi")
    private LocalDateTime siparisTarihi = LocalDateTime.now();

    @Column(name = "guncelleme_tarihi")
    private LocalDateTime guncellemeTarihi = LocalDateTime.now();

    // Constructors
    public SiparisFisi() {
    }

    public SiparisFisi(Kullanici kullanici, Magaza magaza, BigDecimal toplamTutar, String teslimatAdresi) {
        this.kullanici = kullanici;
        this.magaza = magaza;
        this.toplamTutar = toplamTutar;
        this.teslimatAdresi = teslimatAdresi;
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

    public Magaza getMagaza() {
        return magaza;
    }

    public void setMagaza(Magaza magaza) {
        this.magaza = magaza;
    }

    public BigDecimal getToplamTutar() {
        return toplamTutar;
    }

    public void setToplamTutar(BigDecimal toplamTutar) {
        this.toplamTutar = toplamTutar;
    }

    public String getTeslimatAdresi() {
        return teslimatAdresi;
    }

    public void setTeslimatAdresi(String teslimatAdresi) {
        this.teslimatAdresi = teslimatAdresi;
    }

    public SiparisDurum getDurum() {
        return durum;
    }

    public void setDurum(SiparisDurum durum) {
        this.durum = durum;
    }

    public LocalDateTime getSiparisTarihi() {
        return siparisTarihi;
    }

    public void setSiparisTarihi(LocalDateTime siparisTarihi) {
        this.siparisTarihi = siparisTarihi;
    }

    public LocalDateTime getGuncellemeTarihi() {
        return guncellemeTarihi;
    }

    public void setGuncellemeTarihi(LocalDateTime guncellemeTarihi) {
        this.guncellemeTarihi = guncellemeTarihi;
    }

    @PreUpdate
    public void preUpdate() {
        this.guncellemeTarihi = LocalDateTime.now();
    }
}
