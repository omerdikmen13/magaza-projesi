package com.magazaapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "siparis_detay")
public class SiparisDetay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "siparis_fisi_id", nullable = false)
    private SiparisFisi siparisFisi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "urun_id", nullable = false)
    private Urun urun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beden_id", nullable = false)
    private Beden beden;

    @Column(nullable = false)
    private Integer adet;

    @Column(name = "birim_fiyat", nullable = false, precision = 10, scale = 2)
    private BigDecimal birimFiyat;

    @Column(name = "toplam_fiyat", nullable = false, precision = 10, scale = 2)
    private BigDecimal toplamFiyat;

    // Constructors
    public SiparisDetay() {
    }

    public SiparisDetay(SiparisFisi siparisFisi, Urun urun, Beden beden, Integer adet, BigDecimal birimFiyat) {
        this.siparisFisi = siparisFisi;
        this.urun = urun;
        this.beden = beden;
        this.adet = adet;
        this.birimFiyat = birimFiyat;
        this.toplamFiyat = birimFiyat.multiply(BigDecimal.valueOf(adet));
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SiparisFisi getSiparisFisi() {
        return siparisFisi;
    }

    public void setSiparisFisi(SiparisFisi siparisFisi) {
        this.siparisFisi = siparisFisi;
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

    public BigDecimal getBirimFiyat() {
        return birimFiyat;
    }

    public void setBirimFiyat(BigDecimal birimFiyat) {
        this.birimFiyat = birimFiyat;
    }

    public BigDecimal getToplamFiyat() {
        return toplamFiyat;
    }

    public void setToplamFiyat(BigDecimal toplamFiyat) {
        this.toplamFiyat = toplamFiyat;
    }
}
