package com.magazaapp.service;

import com.magazaapp.model.Kullanici;
import com.magazaapp.model.Mesaj;
import com.magazaapp.repository.MesajRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * MesajService - Mevcut model yapısına uyarlandı
 * Model: Mesaj -> gonderen, musteri (muhattap), magaza, gonderenMusteri (flag)
 * Bu yapı mağaza-müşteri mesajlaşmaları için tasarlanmış
 */
@Service
public class MesajService {

    private final MesajRepository mesajRepository;

    public MesajService(MesajRepository mesajRepository) {
        this.mesajRepository = mesajRepository;
    }

    /**
     * Mesaj gönder (mağaza-müşteri arası)
     */
    @Transactional
    public Mesaj mesajGonder(Kullanici gonderen, Kullanici musteri, Long magazaId, String icerik,
            Boolean gonderenMusteri) {
        // NOT: Bu metot şu anda kullanılmıyor çünkü mevcut repository ve model yapısı
        // farklı
        // Gelecekte ihtiyaç duyulursa güncelleme yapılabilir
        throw new UnsupportedOperationException(
                "Bu metot mevcut model yapısıyla uyumlu değil. Lütfen controller üzerinden mesaj gönderin.");
    }

    /**
     * Mağaza ile müşteri arasındaki mesajları getir
     */
    public List<Mesaj> getKonusma(Long magazaId, Long musteriId) {
        return mesajRepository.findByMagazaIdAndMusteriIdOrderByTarihAsc(magazaId, musteriId);
    }

    /**
     * Müşterinin tüm mesajlarını getir
     */
    public List<Mesaj> getMusteriMesajlari(Long musteriId) {
        return mesajRepository.findByMusteriIdOrderByTarihDesc(musteriId);
    }

    /**
     * Mağazaya gelen tüm mesajları getir
     */
    public List<Mesaj> getMagazaMesajlari(Long magazaId) {
        return mesajRepository.findByMagazaIdOrderByTarihDesc(magazaId);
    }

    /**
     * Mesajı okundu olarak işaretle
     */
    @Transactional
    public void mesajOkunduIsaretle(Long mesajId) {
        Mesaj mesaj = mesajRepository.findById(mesajId)
                .orElseThrow(() -> new RuntimeException("Mesaj bulunamadı"));
        mesaj.setOkundu(true);
        mesajRepository.save(mesaj);
    }

    /**
     * Mağazanın okunmamış mesaj sayısını getir
     */
    public long getMagazaOkunmamisMesajSayisi(Long magazaId) {
        return mesajRepository.countOkunmamisByMagazaId(magazaId);
    }

    /**
     * Mesaj sil
     */
    @Transactional
    public void mesajSil(Long mesajId) {
        mesajRepository.deleteById(mesajId);
    }

    /**
     * Mağazaya mesaj gönderen benzersiz müşterileri getir
     */
    public List<Kullanici> getMagazaMusterileri(Long magazaId) {
        return mesajRepository.findDistinctMusterilerByMagazaId(magazaId);
    }

    /**
     * Tüm mesajları getir (admin için)
     */
    public List<Mesaj> getTumMesajlar() {
        return mesajRepository.findAllByOrderByTarihDesc();
    }

    /**
     * Müşterinin mağaza bazında son mesajlarını getir
     */
    public java.util.Map<Long, Mesaj> getMusteriMagazaSonMesajlari(Long musteriId) {
        List<Mesaj> mesajlar = mesajRepository.findByMusteriIdOrderByTarihDesc(musteriId);
        java.util.Map<Long, Mesaj> magazaSonMesaj = new java.util.LinkedHashMap<>();
        for (Mesaj m : mesajlar) {
            magazaSonMesaj.putIfAbsent(m.getMagaza().getId(), m);
        }
        return magazaSonMesaj;
    }

    /**
     * Mağaza-müşteri sohbetini getir ve isteğe bağlı okundu işaretle
     */
    @Transactional
    public List<Mesaj> getMagazaMusteriSohbet(Long magazaId, Long musteriId, boolean okunduIsaretle) {
        List<Mesaj> mesajlar = mesajRepository.findByMagazaIdAndMusteriIdOrderByTarihAsc(magazaId, musteriId);

        if (okunduIsaretle) {
            for (Mesaj m : mesajlar) {
                if (!m.getOkundu()) {
                    m.setOkundu(true);
                    mesajRepository.save(m);
                }
            }
        }

        return mesajlar;
    }

    /**
     * Müşteriden mesaj gönder
     */
    @Transactional
    public void musteridenMesajGonder(Long musteriId, Long magazaId, String icerik) {
        // Controller'dan kullanılacak - repository kullanımı controller'da kalıyor
        throw new UnsupportedOperationException("Bu metot controller'da implement edilecek");
    }

    /**
     * Mağazadan mesaj gönder
     */
    @Transactional
    public void magazadanMesajGonder(Long magazaSahibiId, Long magazaId, Long musteriId, String icerik) {
        // Controller'dan kullanılacak - repository kullanımı controller'da kalıyor
        throw new UnsupportedOperationException("Bu metot controller'da implement edilecek");
    }

    /**
     * Sahip mağazalar için okunmamış mesaj sayıları
     */
    public java.util.Map<com.magazaapp.model.Magaza, Long> getSahipMagazaOkunmamisSayilari(Long sahipId) {
        // Controller'da implement edilecek - repository logic gerekiyor
        throw new UnsupportedOperationException("Bu metot controller'da implement edilecek");
    }
}
