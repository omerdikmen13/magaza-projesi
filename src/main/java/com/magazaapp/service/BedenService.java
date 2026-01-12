package com.magazaapp.service;

import com.magazaapp.model.Beden;
import com.magazaapp.repository.BedenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BedenService {

    private final BedenRepository bedenRepository;

    public BedenService(BedenRepository bedenRepository) {
        this.bedenRepository = bedenRepository;
    }

    /**
     * Tüm bedenleri getir
     */
    public List<Beden> getTumBedenler() {
        return bedenRepository.findAll();
    }

    /**
     * ID'ye göre beden getir
     */
    public Beden getBedenById(Long id) {
        return bedenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Beden bulunamadı"));
    }

    /**
     * Beden adına göre beden bul
     */
    public Beden getBedenByAd(String ad) {
        return bedenRepository.findByAd(ad)
                .orElseThrow(() -> new RuntimeException("Beden bulunamadı: " + ad));
    }

    /**
     * Beden kaydet
     */
    @Transactional
    public Beden saveBeden(Beden beden) {
        return bedenRepository.save(beden);
    }

    /**
     * Yeni beden oluştur
     */
    @Transactional
    public Beden createBeden(String ad) {
        Beden beden = new Beden(ad);
        return bedenRepository.save(beden);
    }

    /**
     * Beden sil
     */
    @Transactional
    public void deleteBeden(Long id) {
        bedenRepository.deleteById(id);
    }

    /**
     * Beden var mı kontrol et
     */
    public boolean bedenVarMi(String ad) {
        return bedenRepository.findByAd(ad).isPresent();
    }
}
