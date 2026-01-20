package com.magazaapp.service;

import com.magazaapp.dto.SiparisOzetiDTO;
import com.magazaapp.model.*;
import com.magazaapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SiparisService {

    private final SiparisFisiRepository siparisFisiRepository;
    private final SiparisDetayRepository siparisDetayRepository;
    private final SepetRepository sepetRepository;
    private final UrunStokRepository urunStokRepository;
    private final KullaniciRepository kullaniciRepository;
    private final EmailService emailService;

    public SiparisService(SiparisFisiRepository siparisFisiRepository,
            SiparisDetayRepository siparisDetayRepository,
            SepetRepository sepetRepository,
            UrunStokRepository urunStokRepository,
            KullaniciRepository kullaniciRepository,
            EmailService emailService) {
        this.siparisFisiRepository = siparisFisiRepository;
        this.siparisDetayRepository = siparisDetayRepository;
        this.sepetRepository = sepetRepository;
        this.urunStokRepository = urunStokRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.emailService = emailService;
    }

    /**
     * Sipariş oluştur - sepetten
     */
    @Transactional
    public SiparisResult siparisOlustur(Kullanici kullanici) {
        List<Sepet> sepetListesi = sepetRepository.findByKullaniciId(kullanici.getId());

        if (sepetListesi.isEmpty()) {
            return SiparisResult.hata("Sepetiniz boş!");
        }

        // Mağaza ID'sini al (sepetteki ilk ürünün mağazası)
        Magaza magaza = sepetListesi.get(0).getUrun().getMagaza();

        // Farklı mağaza kontrolü - tüm ürünler aynı mağazadan olmalı
        for (Sepet sepetItem : sepetListesi) {
            if (!sepetItem.getUrun().getMagaza().getId().equals(magaza.getId())) {
                return SiparisResult.hata(
                        "Sipariş oluşturulamadı: Sepetinizde farklı mağazalardan ürünler var. " +
                                "Lütfen tek mağazadan sipariş verin.");
            }
        }

        // ÖNCELİKLİ STOK KONTROLÜ - Sipariş vermeden önce tüm stokları kontrol et
        for (Sepet sepetItem : sepetListesi) {
            UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(
                    sepetItem.getUrun().getId(), sepetItem.getBeden().getId())
                    .orElseThrow(() -> new RuntimeException("Stok bulunamadı: " + sepetItem.getUrun().getAd()));

            if (stok.getAdet() < sepetItem.getAdet()) {
                return SiparisResult.hata(
                        "Yetersiz stok! '" + sepetItem.getUrun().getAd() + "' ürününden " +
                                stok.getAdet() + " adet kaldı, sepetinizde " + sepetItem.getAdet() + " adet var.");
            }
        }

        // Toplam tutarı hesapla
        BigDecimal toplamTutar = sepetListesi.stream()
                .map(item -> item.getUrun().getFiyat().multiply(BigDecimal.valueOf(item.getAdet())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sipariş fişi oluştur
        SiparisFisi siparisFisi = new SiparisFisi();
        siparisFisi.setKullanici(kullanici);
        siparisFisi.setMagaza(magaza);
        siparisFisi.setToplamTutar(toplamTutar);
        siparisFisi.setTeslimatAdresi(kullanici.getAdres() != null ? kullanici.getAdres() : "Adres yok");
        siparisFisi.setDurum(SiparisDurum.BEKLEMEDE);
        siparisFisi = siparisFisiRepository.save(siparisFisi);

        // Sipariş detaylarını oluştur ve stokları düş
        for (Sepet sepetItem : sepetListesi) {
            SiparisDetay detay = new SiparisDetay();
            detay.setSiparisFisi(siparisFisi);
            detay.setUrun(sepetItem.getUrun());
            detay.setBeden(sepetItem.getBeden());
            detay.setAdet(sepetItem.getAdet());
            detay.setBirimFiyat(sepetItem.getUrun().getFiyat());
            detay.setToplamFiyat(sepetItem.getUrun().getFiyat().multiply(BigDecimal.valueOf(sepetItem.getAdet())));
            siparisDetayRepository.save(detay);

            // Stok düş (güvenli - yukarıda kontrol edildi)
            UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(
                    sepetItem.getUrun().getId(), sepetItem.getBeden().getId())
                    .orElseThrow(() -> new RuntimeException("Stok bulunamadı"));

            int yeniStok = stok.getAdet() - sepetItem.getAdet();
            if (yeniStok < 0) {
                throw new RuntimeException("Stok hatası: Negatif stok oluşamaz!");
            }
            stok.setAdet(yeniStok);
            urunStokRepository.save(stok);
        }

        // Sepeti boşalt
        sepetRepository.deleteByKullaniciId(kullanici.getId());

        // ✅ SİPARİŞ ÖZETİ MAİLİ GÖNDER
        if (kullanici.getEmail() != null && !kullanici.getEmail().isEmpty()) {
            // Sipariş detaylarını al
            List<SiparisDetay> detaylar = siparisDetayRepository.findBySiparisFisiId(siparisFisi.getId());
            SiparisOzetiDTO ozet = siparisOzetiOlustur(siparisFisi, detaylar);
            emailService.siparisOzetiMailiGonder(ozet);
        }

        return SiparisResult.basarili("Siparişiniz başarıyla oluşturuldu! Sipariş No: #" + siparisFisi.getId(),
                siparisFisi.getId());
    }

    /**
     * Sipariş özeti DTO oluştur (private helper)
     */
    private SiparisOzetiDTO siparisOzetiOlustur(SiparisFisi fisi, List<SiparisDetay> detaylar) {
        SiparisOzetiDTO ozet = new SiparisOzetiDTO();
        ozet.setSiparisId(fisi.getId());

        String musteriAd = (fisi.getKullanici().getAd() != null ? fisi.getKullanici().getAd() : "") +
                " " +
                (fisi.getKullanici().getSoyad() != null ? fisi.getKullanici().getSoyad() : "");
        ozet.setMusteriAd(musteriAd.trim().isEmpty() ? fisi.getKullanici().getKullaniciAdi() : musteriAd.trim());
        ozet.setMusteriEmail(fisi.getKullanici().getEmail());
        ozet.setMagazaAd(fisi.getMagaza().getAd());
        ozet.setToplamTutar(fisi.getToplamTutar());
        ozet.setTeslimatAdresi(fisi.getTeslimatAdresi());
        ozet.setSiparisTarihi(fisi.getSiparisTarihi().format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        List<SiparisOzetiDTO.SiparisKalemiDTO> kalemler = new ArrayList<>();
        for (SiparisDetay d : detaylar) {
            kalemler.add(new SiparisOzetiDTO.SiparisKalemiDTO(
                    d.getUrun().getAd(),
                    d.getBeden().getAd(),
                    d.getAdet(),
                    d.getBirimFiyat(),
                    d.getBirimFiyat().multiply(BigDecimal.valueOf(d.getAdet()))));
        }
        ozet.setKalemler(kalemler);

        return ozet;
    }

    /**
     * Kullanıcının siparişlerini getir
     */
    public List<SiparisFisi> getKullaniciSiparisleri(Long kullaniciId) {
        return siparisFisiRepository.findByKullaniciIdOrderBySiparisTarihiDesc(kullaniciId);
    }

    /**
     * Mağazanın siparişlerini getir
     */
    public List<SiparisFisi> getMagazaSiparisleri(Long magazaId) {
        return siparisFisiRepository.findByMagazaIdOrderBySiparisTarihiDesc(magazaId);
    }

    /**
     * Sipariş detayını getir
     */
    public SiparisFisi getSiparisById(Long siparisId) {
        return siparisFisiRepository.findById(siparisId)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));
    }

    /**
     * Sipariş durumunu güncelle
     */
    @Transactional
    public void siparisDurumGuncelle(Long siparisId, SiparisDurum yeniDurum) {
        SiparisFisi siparis = getSiparisById(siparisId);
        siparis.setDurum(yeniDurum);
        siparisFisiRepository.save(siparis);
    }

    /**
     * Sipariş detaylarını getir
     */
    public List<SiparisDetay> getSiparisDetaylari(Long siparisId) {
        return siparisDetayRepository.findBySiparisFisiId(siparisId);
    }

    /**
     * Duruma göre mağaza siparişlerini getir
     */
    public List<SiparisFisi> getMagazaSiparislerByDurum(Long magazaId, SiparisDurum durum) {
        return siparisFisiRepository.findByMagazaIdAndDurum(magazaId, durum);
    }

    /**
     * Tüm siparişleri getir (Admin için)
     */
    public List<SiparisFisi> getTumSiparisler() {
        return siparisFisiRepository.findAllByOrderBySiparisTarihiDesc();
    }

    // Inner class - Sipariş sonucu
    public static class SiparisResult {
        private boolean success;
        private String mesaj;
        private Long siparisId;

        public static SiparisResult basarili(String mesaj, Long siparisId) {
            SiparisResult result = new SiparisResult();
            result.success = true;
            result.mesaj = mesaj;
            result.siparisId = siparisId;
            return result;
        }

        public static SiparisResult hata(String mesaj) {
            SiparisResult result = new SiparisResult();
            result.success = false;
            result.mesaj = mesaj;
            return result;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMesaj() {
            return mesaj;
        }

        public Long getSiparisId() {
            return siparisId;
        }
    }

    /**
     * Sepetten sipariş oluştur (wrapper for controller compatibility)
     */
    @Transactional
    public SiparisFisi sepettenSiparisOlustur(Long kullaniciId) {
        Kullanici kullanici = kullaniciRepository.findById(kullaniciId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        SiparisResult result = siparisOlustur(kullanici);

        if (!result.isSuccess()) {
            throw new RuntimeException(result.getMesaj());
        }

        return getSiparisById(result.getSiparisId());
    }
}
