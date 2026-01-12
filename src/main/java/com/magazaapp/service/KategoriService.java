package com.magazaapp.service;

import com.magazaapp.model.AltKategori;
import com.magazaapp.model.Kategori;
import com.magazaapp.repository.AltKategoriRepository;
import com.magazaapp.repository.KategoriRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KategoriService {

    private final KategoriRepository kategoriRepository;
    private final AltKategoriRepository altKategoriRepository;

    public KategoriService(KategoriRepository kategoriRepository, AltKategoriRepository altKategoriRepository) {
        this.kategoriRepository = kategoriRepository;
        this.altKategoriRepository = altKategoriRepository;
    }

    /**
     * Tüm kategorileri getir
     */
    public List<Kategori> getTumKategoriler() {
        return kategoriRepository.findAll();
    }

    /**
     * ID'ye göre kategori getir
     */
    public Kategori getKategoriById(Long id) {
        return kategoriRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kategori bulunamadı"));
    }

    /**
     * ID'ye göre alt kategori getir
     */
    public AltKategori getAltKategoriById(Long id) {
        return altKategoriRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alt kategori bulunamadı"));
    }

    /**
     * Kategori kaydet
     */
    @Transactional
    public Kategori saveKategori(Kategori kategori) {
        return kategoriRepository.save(kategori);
    }

    /**
     * Yeni kategori oluştur
     */
    @Transactional
    public Kategori createKategori(String ad) {
        Kategori kategori = new Kategori(ad);
        return kategoriRepository.save(kategori);
    }

    /**
     * Kategori güncelle
     */
    @Transactional
    public Kategori updateKategori(Long id, String yeniAd) {
        Kategori kategori = getKategoriById(id);
        kategori.setAd(yeniAd);
        return kategoriRepository.save(kategori);
    }

    /**
     * Kategori sil
     */
    @Transactional
    public void deleteKategori(Long id) {
        kategoriRepository.deleteById(id);
    }

    /**
     * Kategori sayısını getir
     */
    public long getKategoriSayisi() {
        return kategoriRepository.count();
    }
}
