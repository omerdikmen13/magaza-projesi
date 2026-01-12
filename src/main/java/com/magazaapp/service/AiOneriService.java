package com.magazaapp.service;

import com.magazaapp.model.AiOneri;
import com.magazaapp.model.Kullanici;
import com.magazaapp.repository.AiOneriRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AiOneriService {

    private final AiOneriRepository aiOneriRepository;
    private final GeminiService geminiService;

    public AiOneriService(AiOneriRepository aiOneriRepository, GeminiService geminiService) {
        this.aiOneriRepository = aiOneriRepository;
        this.geminiService = geminiService;
    }

    /**
     * Tüm AI önerilerini getir
     */
    public List<AiOneri> getTumOnerileri() {
        return aiOneriRepository.findAll();
    }

    /**
     * ID'ye göre AI önerisi getir
     */
    public AiOneri getOneriById(Long id) {
        return aiOneriRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI önerisi bulunamadı"));
    }

    /**
     * Kullanıcıya göre AI önerilerini getir (tarihe göre sıralı)
     */
    public List<AiOneri> getKullaniciOnerileri(Long kullaniciId) {
        return aiOneriRepository.findByKullaniciIdOrderByTarihDesc(kullaniciId);
    }

    /**
     * AI önerisi kaydet
     */
    @Transactional
    public AiOneri saveOneri(AiOneri aiOneri) {
        return aiOneriRepository.save(aiOneri);
    }

    /**
     * Yeni AI önerisi oluştur ve kaydet
     */
    @Transactional
    public AiOneri createOneri(Kullanici kullanici, String soru) {
        // AI'dan cevap al
        String cevap = geminiService.metinUret(soru);

        AiOneri oneri = new AiOneri(kullanici, soru, cevap);
        oneri.setTarih(LocalDateTime.now());

        return aiOneriRepository.save(oneri);
    }

    /**
     * AI önerisi sil
     */
    @Transactional
    public void deleteOneri(Long id) {
        aiOneriRepository.deleteById(id);
    }

    /**
     * Kullanıcının tüm AI geçmişini sil
     */
    @Transactional
    public void deleteKullaniciTumOnerileri(Long kullaniciId) {
        List<AiOneri> oneriler = getKullaniciOnerileri(kullaniciId);
        aiOneriRepository.deleteAll(oneriler);
    }

    /**
     * Kullanıcının AI öneri sayısını getir
     */
    public long getKullaniciOneriSayisi(Long kullaniciId) {
        return getKullaniciOnerileri(kullaniciId).size();
    }

    /**
     * En son AI önerisini getir
     */
    public AiOneri getKullaniciEnSonOneri(Long kullaniciId) {
        List<AiOneri> oneriler = getKullaniciOnerileri(kullaniciId);
        if (oneriler.isEmpty()) {
            throw new RuntimeException("Kullanıcının AI önerisi bulunamadı");
        }
        return oneriler.get(0); // İlk eleman en yeni (tarih desc)
    }

    /**
     * AI soru-cevap geçmişini text formatında getir
     */
    public String getKullaniciGecmisMetin(Long kullaniciId) {
        List<AiOneri> oneriler = getKullaniciOnerileri(kullaniciId);
        StringBuilder sb = new StringBuilder();

        for (AiOneri oneri : oneriler) {
            sb.append("Tarih: ").append(oneri.getTarih()).append("\n");
            sb.append("Soru: ").append(oneri.getSoru()).append("\n");
            sb.append("Cevap: ").append(oneri.getCevap()).append("\n");
            sb.append("---\n");
        }

        return sb.toString();
    }
}
