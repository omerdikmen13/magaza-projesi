package com.magazaapp.service;

import com.magazaapp.model.Magaza;
import com.magazaapp.model.Urun;
import com.magazaapp.repository.MagazaRepository;
import com.magazaapp.repository.UrunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MagazaService {

    private final MagazaRepository magazaRepository;
    private final UrunRepository urunRepository;

    public MagazaService(MagazaRepository magazaRepository, UrunRepository urunRepository) {
        this.magazaRepository = magazaRepository;
        this.urunRepository = urunRepository;
    }

    /**
     * Tüm aktif mağazaları getir
     */
    public List<Magaza> getTumMagazalar() {
        return magazaRepository.findAll();
    }

    /**
     * Mağaza sahibine göre mağazayı getir
     */
    public Magaza getMagazaBySahip(Long sahipId) {
        List<Magaza> magazalar = magazaRepository.findBySahipId(sahipId);
        if (magazalar.isEmpty()) {
            throw new RuntimeException("Mağaza sahibine ait mağaza bulunamadı");
        }
        return magazalar.get(0); // İlk mağazayı döndür
    }

    /**
     * Mağaza detayını getir
     */
    public Magaza getMagazaById(Long id) {
        return magazaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));
    }

    /**
     * Mağaza kaydet/güncelle
     */
    @Transactional
    public Magaza saveMagaza(Magaza magaza) {
        return magazaRepository.save(magaza);
    }

    /**
     * Mağazanın ürünlerini getir
     */
    public List<Urun> getMagazaninUrunleri(Long magazaId) {
        return urunRepository.findByMagazaIdAndAktifTrue(magazaId);
    }

    /**
     * Mağaza adıyla ara
     */
    public List<Magaza> magazaAra(String aramaKelimesi) {
        if (aramaKelimesi == null || aramaKelimesi.trim().isEmpty()) {
            return List.of();
        }
        // Basit arama - repository'de query method tanımlanabilir
        return magazaRepository.findAll().stream()
                .filter(m -> m.getAd().toLowerCase().contains(aramaKelimesi.toLowerCase()))
                .toList();
    }

    /**
     * Sahibe göre tüm mağazaları getir (liste)
     */
    public List<Magaza> getMagazalarBySahip(Long sahipId) {
        return magazaRepository.findBySahipId(sahipId);
    }

    /**
     * Mağaza kaydet (alias for save)
     */
    @Transactional
    public Magaza kaydet(Magaza magaza) {
        return magazaRepository.save(magaza);
    }

    /**
     * Mağaza sil
     */
    @Transactional
    public void sil(Long id) {
        magazaRepository.deleteById(id);
    }
}
