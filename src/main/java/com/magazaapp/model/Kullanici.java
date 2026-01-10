package com.magazaapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kullanici")
public class Kullanici {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kullanici_adi", nullable = false, unique = true, length = 50)
    private String kullaniciAdi;

    @Column(unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String sifre;

    @Column(length = 50)
    private String ad;

    @Column(length = 50)
    private String soyad;

    @Column(length = 20)
    private String telefon;

    @Column(columnDefinition = "TEXT")
    private String adres;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KullaniciRol rol = KullaniciRol.MUSTERI;

    @Column(nullable = false)
    private Boolean aktif = true;

    @Column(name = "olusturma_tarihi")
    private LocalDateTime olusturmaTarihi = LocalDateTime.now();

    // Constructors
    public Kullanici() {
    }

    public Kullanici(String kullaniciAdi, String email, String sifre, KullaniciRol rol) {
        this.kullaniciAdi = kullaniciAdi;
        this.email = email;
        this.sifre = sifre;
        this.rol = rol;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKullaniciAdi() {
        return kullaniciAdi;
    }

    public void setKullaniciAdi(String kullaniciAdi) {
        this.kullaniciAdi = kullaniciAdi;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSifre() {
        return sifre;
    }

    public void setSifre(String sifre) {
        this.sifre = sifre;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public String getSoyad() {
        return soyad;
    }

    public void setSoyad(String soyad) {
        this.soyad = soyad;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getAdres() {
        return adres;
    }

    public void setAdres(String adres) {
        this.adres = adres;
    }

    public KullaniciRol getRol() {
        return rol;
    }

    public void setRol(KullaniciRol rol) {
        this.rol = rol;
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
