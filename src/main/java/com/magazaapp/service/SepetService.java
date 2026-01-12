package com.magazaapp.service;

import com.magazaapp.model.*;
import com.magazaapp.repository.BedenRepository;
import com.magazaapp.repository.SepetRepository;
import com.magazaapp.repository.UrunRepository;
import com.magazaapp.repository.UrunStokRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SepetService {

    private final SepetRepository sepetRepository;
    private final UrunRepository urunRepository;
    private final UrunStokRepository urunStokRepository;
    private final BedenRepository bedenRepository;

    public SepetService(SepetRepository sepetRepository,
            UrunRepository urunRepository,
            UrunStokRepository urunStokRepository,
            BedenRepository bedenRepository) {
        this.sepetRepository = sepetRepository;
        this.urunRepository = urunRepository;
        this.urunStokRepository = urunStokRepository;
        this.bedenRepository = bedenRepository;
    }

    /**
     * Kullanıcının sepetini getir
     */
    public List<Sepet> getKullaniciSepeti(Long kullaniciId) {
        return sepetRepository.findByKullaniciId(kullaniciId);
    }

    /**
     * Sepete ürün ekle - farklı mağaza kontrolü ile
     */
    @Transactional
    public SepetEklemeResult sepeteEkle(Kullanici kullanici, Long urunId, Long bedenId,
            int adet, Boolean farkliMagazaOnay) {

        Urun urun = urunRepository.findById(urunId)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

        Beden beden = bedenRepository.findById(bedenId)
                .orElseThrow(() -> new RuntimeException("Beden bulunamadı"));

        // Farklı mağaza kontrolü
        List<Sepet> mevcutSepet = sepetRepository.findByKullaniciId(kullanici.getId());
        if (!mevcutSepet.isEmpty()) {
            Long sepettekiMagazaId = mevcutSepet.get(0).getUrun().getMagaza().getId();
            if (!sepettekiMagazaId.equals(urun.getMagaza().getId())) {
                // Onay verilmemişse uyarı döndür
                if (farkliMagazaOnay == null || !farkliMagazaOnay) {
                    SepetEklemeResult result = new SepetEklemeResult();
                    result.setSuccess(false);
                    result.setFarkliMagazaUyari(true);
                    result.setMevcutMagazaAd(mevcutSepet.get(0).getUrun().getMagaza().getAd());
                    result.setYeniMagazaAd(urun.getMagaza().getAd());
                    return result;
                } else {
                    // Onay verilmiş, sepeti boşalt
                    sepetRepository.deleteByKullaniciId(kullanici.getId());
                    mevcutSepet.clear();
                }
            }
        }

        // Stok kontrolü
        UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(urunId, bedenId)
                .orElseThrow(() -> new RuntimeException("Stok bulunamadı"));

        // Sepette zaten var mı?
        Sepet sepetItem = sepetRepository.findByKullaniciIdAndUrunIdAndBedenId(
                kullanici.getId(), urunId, bedenId)
                .orElse(new Sepet());

        // Toplam miktar kontrolü (mevcut sepetteki + yeni eklenen)
        int mevcutSepetMiktari = (sepetItem.getId() != null) ? sepetItem.getAdet() : 0;
        int toplamMiktar = mevcutSepetMiktari + adet;

        if (stok.getAdet() < toplamMiktar) {
            SepetEklemeResult result = new SepetEklemeResult();
            result.setSuccess(false);
            String mesaj = mevcutSepetMiktari > 0
                    ? "Yeterli stok yok! Sepetinizde zaten " + mevcutSepetMiktari + " adet var. Maksimum "
                            + stok.getAdet() + " adet ekleyebilirsiniz."
                    : "Yeterli stok yok! Maksimum " + stok.getAdet() + " adet ekleyebilirsiniz.";
            result.setHataMesaji(mesaj);
            return result;
        }

        if (sepetItem.getId() == null) {
            sepetItem.setKullanici(kullanici);
            sepetItem.setUrun(urun);
            sepetItem.setBeden(beden);
            sepetItem.setAdet(adet);
        } else {
            sepetItem.setAdet(sepetItem.getAdet() + adet);
        }

        sepetRepository.save(sepetItem);

        SepetEklemeResult result = new SepetEklemeResult();
        result.setSuccess(true);
        result.setBasariMesaji("Ürün sepete eklendi!");
        return result;
    }

    /**
     * Sepetten ürün sil
     */
    @Transactional
    public void sepettenSil(Long sepetId) {
        sepetRepository.deleteById(sepetId);
    }

    /**
     * Sepet miktarını güncelle
     */
    @Transactional
    public void sepetGuncelle(Long sepetId, int yeniAdet) {
        Sepet sepet = sepetRepository.findById(sepetId)
                .orElseThrow(() -> new RuntimeException("Sepet öğesi bulunamadı"));

        // Stok kontrolü
        UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(
                sepet.getUrun().getId(), sepet.getBeden().getId())
                .orElseThrow(() -> new RuntimeException("Stok bulunamadı"));

        if (stok.getAdet() < yeniAdet) {
            throw new RuntimeException("Yeterli stok yok! Maksimum " + stok.getAdet() + " adet ekleyebilirsiniz.");
        }

        sepet.setAdet(yeniAdet);
        sepetRepository.save(sepet);
    }

    /**
     * Sepeti tamamen boşalt
     */
    @Transactional
    public void sepetiBosalt(Long kullaniciId) {
        sepetRepository.deleteByKullaniciId(kullaniciId);
    }

    /**
     * Sepet toplamını hesapla
     */
    public SepetOzet getSepetOzeti(Long kullaniciId) {
        List<Sepet> sepetListesi = sepetRepository.findByKullaniciId(kullaniciId);

        SepetOzet ozet = new SepetOzet();
        ozet.setToplamUrunSayisi(sepetListesi.size());
        ozet.setToplamAdet(sepetListesi.stream().mapToInt(Sepet::getAdet).sum());

        // Toplam fiyat hesapla
        double toplam = sepetListesi.stream()
                .mapToDouble(s -> s.getUrun().getFiyat().doubleValue() * s.getAdet())
                .sum();
        ozet.setToplamFiyat(toplam);

        return ozet;
    }

    // Inner class - Sepet ekleme sonucu
    public static class SepetEklemeResult {
        private boolean success;
        private String basariMesaji;
        private String hataMesaji;
        private boolean farkliMagazaUyari;
        private String mevcutMagazaAd;
        private String yeniMagazaAd;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getBasariMesaji() {
            return basariMesaji;
        }

        public void setBasariMesaji(String basariMesaji) {
            this.basariMesaji = basariMesaji;
        }

        public String getHataMesaji() {
            return hataMesaji;
        }

        public void setHataMesaji(String hataMesaji) {
            this.hataMesaji = hataMesaji;
        }

        public boolean isFarkliMagazaUyari() {
            return farkliMagazaUyari;
        }

        public void setFarkliMagazaUyari(boolean farkliMagazaUyari) {
            this.farkliMagazaUyari = farkliMagazaUyari;
        }

        public String getMevcutMagazaAd() {
            return mevcutMagazaAd;
        }

        public void setMevcutMagazaAd(String mevcutMagazaAd) {
            this.mevcutMagazaAd = mevcutMagazaAd;
        }

        public String getYeniMagazaAd() {
            return yeniMagazaAd;
        }

        public void setYeniMagazaAd(String yeniMagazaAd) {
            this.yeniMagazaAd = yeniMagazaAd;
        }
    }

    // Inner class - Sepet özeti
    public static class SepetOzet {
        private int toplamUrunSayisi;
        private int toplamAdet;
        private double toplamFiyat;

        public int getToplamUrunSayisi() {
            return toplamUrunSayisi;
        }

        public void setToplamUrunSayisi(int toplamUrunSayisi) {
            this.toplamUrunSayisi = toplamUrunSayisi;
        }

        public int getToplamAdet() {
            return toplamAdet;
        }

        public void setToplamAdet(int toplamAdet) {
            this.toplamAdet = toplamAdet;
        }

        public double getToplamFiyat() {
            return toplamFiyat;
        }

        public void setToplamFiyat(double toplamFiyat) {
            this.toplamFiyat = toplamFiyat;
        }
    }

    // ============ CONTROLLER COMPAT METHODS ============

    public List<Sepet> getSepetByKullanici(Long kullaniciId) {
        return getKullaniciSepeti(kullaniciId);
    }

    public java.math.BigDecimal getSepetToplam(Long kullaniciId) {
        List<Sepet> sepet = getSepetByKullanici(kullaniciId);
        return sepet.stream()
                .map(item -> item.getUrun().getFiyat().multiply(java.math.BigDecimal.valueOf(item.getAdet())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }

    public void sepetItemSil(Long sepetItemId) {
        sepettenSil(sepetItemId);
    }

    public void sepetItemGuncelle(Long sepetItemId, int yeniAdet) {
        sepetGuncelle(sepetItemId, yeniAdet);
    }

    public void sepetiTemizle(Long kullaniciId) {
        sepetiBosalt(kullaniciId);
    }
}
