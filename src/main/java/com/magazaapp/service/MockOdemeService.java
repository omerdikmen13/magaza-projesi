package com.magazaapp.service;

import com.magazaapp.model.*;
import com.magazaapp.repository.OdemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * MOCK Ödeme Servisi
 * Gerçek ödeme sistemi yerine simüle edilmiş ödeme
 * API Key gerektirmez - Demo/Tez için ideal
 */
@Service
public class MockOdemeService {

    private final OdemeRepository odemeRepository;
    private final SepetService sepetService;

    // Test kart numaraları
    private static final String TEST_CARD_SUCCESS = "4111111111111111"; // Visa test
    private static final String TEST_CARD_SUCCESS_2 = "5528790000000008"; // MC test
    private static final String TEST_CARD_FAIL = "4000000000000002"; // Her zaman başarısız

    public MockOdemeService(OdemeRepository odemeRepository, SepetService sepetService) {
        this.odemeRepository = odemeRepository;
        this.sepetService = sepetService;
    }

    /**
     * Ödeme başlat - Mock token oluştur
     */
    @Transactional
    public OdemeBaslatSonuc odemeBaslat(Kullanici kullanici) {
        List<Sepet> sepetListesi = sepetService.getKullaniciSepeti(kullanici.getId());
        if (sepetListesi.isEmpty()) {
            throw new RuntimeException("Sepetiniz boş!");
        }

        BigDecimal toplamTutar = hesaplaToplam(sepetListesi);
        String token = "MOCK-" + UUID.randomUUID().toString();
        String basketId = "SP-" + System.currentTimeMillis();

        // Ödeme kaydı oluştur
        Odeme odeme = new Odeme(kullanici, toplamTutar);
        odeme.setIyzicoToken(token);
        odeme.setConversationId(UUID.randomUUID().toString());
        odeme.setBasketId(basketId);
        odeme.setDurum(OdemeDurum.BEKLEMEDE);
        odemeRepository.save(odeme);

        OdemeBaslatSonuc sonuc = new OdemeBaslatSonuc();
        sonuc.setSuccess(true);
        sonuc.setToken(token);
        sonuc.setOdemeId(odeme.getId());
        sonuc.setTutar(toplamTutar);

        return sonuc;
    }

    /**
     * Ödeme tamamla - Kart bilgilerini simüle et
     */
    @Transactional
    public OdemeSonuc odemeTamamla(String token, String kartNo, String sonKullanma, String cvv, String kartSahibi) {
        Odeme odeme = odemeRepository.findByIyzicoToken(token)
                .orElseThrow(() -> new RuntimeException("Ödeme bulunamadı!"));

        if (odeme.getDurum() != OdemeDurum.BEKLEMEDE) {
            throw new RuntimeException("Bu ödeme zaten işlenmiş!");
        }

        OdemeSonuc sonuc = new OdemeSonuc();
        sonuc.setOdeme(odeme);

        // Kart numarasını temizle (boşlukları kaldır)
        String temizKartNo = kartNo.replaceAll("\\s+", "");

        // Mock kart kontrolü - gerçek doğrulama yok
        if (temizKartNo.equals(TEST_CARD_FAIL)) {
            // Başarısız ödeme simülasyonu
            odeme.setDurum(OdemeDurum.BASARISIZ);
            odeme.setHataMesaji("Kart reddedildi (Mock)");
            odeme.setTamamlanmaTarihi(LocalDateTime.now());

            sonuc.setBasarili(false);
            sonuc.setMesaj("Ödeme başarısız! Kart reddedildi.");
        } else if (temizKartNo.length() >= 13 && temizKartNo.length() <= 19) {
            // Başarılı ödeme simülasyonu
            odeme.setDurum(OdemeDurum.BASARILI);
            odeme.setIyzicoPaymentId("MOCK-PAY-" + System.currentTimeMillis());
            odeme.setTamamlanmaTarihi(LocalDateTime.now());
            odeme.setKartTipi(getKartTipi(temizKartNo));
            odeme.setKartNoSonDort(temizKartNo.substring(temizKartNo.length() - 4));
            odeme.setTaksitSayisi(1);

            sonuc.setBasarili(true);
            sonuc.setMesaj("Ödeme başarıyla tamamlandı!");
        } else {
            // Geçersiz kart numarası
            odeme.setDurum(OdemeDurum.BASARISIZ);
            odeme.setHataMesaji("Geçersiz kart numarası");
            odeme.setTamamlanmaTarihi(LocalDateTime.now());

            sonuc.setBasarili(false);
            sonuc.setMesaj("Geçersiz kart numarası!");
        }

        odemeRepository.save(odeme);
        return sonuc;
    }

    /**
     * Token ile ödeme durumunu sorgula
     */
    public Odeme getOdemeByToken(String token) {
        return odemeRepository.findByIyzicoToken(token)
                .orElseThrow(() -> new RuntimeException("Ödeme bulunamadı!"));
    }

    /**
     * ID ile ödeme getir
     */
    public Odeme getOdemeById(Long id) {
        return odemeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ödeme bulunamadı!"));
    }

    // ================= HELPER METODLAR =================

    private BigDecimal hesaplaToplam(List<Sepet> sepetListesi) {
        return sepetListesi.stream()
                .map(s -> s.getUrun().getFiyat().multiply(BigDecimal.valueOf(s.getAdet())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String getKartTipi(String kartNo) {
        if (kartNo.startsWith("4"))
            return "VISA";
        if (kartNo.startsWith("5"))
            return "MASTERCARD";
        if (kartNo.startsWith("3"))
            return "AMEX";
        return "OTHER";
    }

    // ================= SONUÇ SINIFLARI =================

    public static class OdemeBaslatSonuc {
        private boolean success;
        private String token;
        private Long odemeId;
        private BigDecimal tutar;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Long getOdemeId() {
            return odemeId;
        }

        public void setOdemeId(Long odemeId) {
            this.odemeId = odemeId;
        }

        public BigDecimal getTutar() {
            return tutar;
        }

        public void setTutar(BigDecimal tutar) {
            this.tutar = tutar;
        }
    }

    public static class OdemeSonuc {
        private boolean basarili;
        private String mesaj;
        private Odeme odeme;

        public boolean isBasarili() {
            return basarili;
        }

        public void setBasarili(boolean basarili) {
            this.basarili = basarili;
        }

        public String getMesaj() {
            return mesaj;
        }

        public void setMesaj(String mesaj) {
            this.mesaj = mesaj;
        }

        public Odeme getOdeme() {
            return odeme;
        }

        public void setOdeme(Odeme odeme) {
            this.odeme = odeme;
        }
    }
}
