package com.magazaapp.dto;

import java.math.BigDecimal;
import java.util.List;

public class SiparisOzetiDTO {
    private Long siparisId;
    private String musteriAd;
    private String musteriEmail;
    private String magazaAd;
    private BigDecimal toplamTutar;
    private String teslimatAdresi;
    private List<SiparisKalemiDTO> kalemler;
    private String siparisTarihi;

    // Inner class for sipariş kalemleri
    public static class SiparisKalemiDTO {
        private String urunAd;
        private String beden;
        private int adet;
        private BigDecimal birimFiyat;
        private BigDecimal toplamFiyat;

        public SiparisKalemiDTO() {
        }

        public SiparisKalemiDTO(String urunAd, String beden, int adet,
                BigDecimal birimFiyat, BigDecimal toplamFiyat) {
            this.urunAd = urunAd;
            this.beden = beden;
            this.adet = adet;
            this.birimFiyat = birimFiyat;
            this.toplamFiyat = toplamFiyat;
        }

        // Getters & Setters
        public String getUrunAd() {
            return urunAd;
        }

        public void setUrunAd(String urunAd) {
            this.urunAd = urunAd;
        }

        public String getBeden() {
            return beden;
        }

        public void setBeden(String beden) {
            this.beden = beden;
        }

        public int getAdet() {
            return adet;
        }

        public void setAdet(int adet) {
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

    // Constructors
    public SiparisOzetiDTO() {
    }

    // Getters & Setters
    public Long getSiparisId() {
        return siparisId;
    }

    public void setSiparisId(Long siparisId) {
        this.siparisId = siparisId;
    }

    public String getMusteriAd() {
        return musteriAd;
    }

    public void setMusteriAd(String musteriAd) {
        this.musteriAd = musteriAd;
    }

    public String getMusteriEmail() {
        return musteriEmail;
    }

    public void setMusteriEmail(String musteriEmail) {
        this.musteriEmail = musteriEmail;
    }

    public String getMagazaAd() {
        return magazaAd;
    }

    public void setMagazaAd(String magazaAd) {
        this.magazaAd = magazaAd;
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

    public List<SiparisKalemiDTO> getKalemler() {
        return kalemler;
    }

    public void setKalemler(List<SiparisKalemiDTO> kalemler) {
        this.kalemler = kalemler;
    }

    public String getSiparisTarihi() {
        return siparisTarihi;
    }

    public void setSiparisTarihi(String siparisTarihi) {
        this.siparisTarihi = siparisTarihi;
    }
}
