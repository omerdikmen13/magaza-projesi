package com.magazaapp.service;

import com.magazaapp.model.AltKategori;
import com.magazaapp.model.Kategori;
import com.magazaapp.model.Sezon;
import com.magazaapp.repository.AltKategoriRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AltKategoriService {

    private final AltKategoriRepository altKategoriRepository;

    public AltKategoriService(AltKategoriRepository altKategoriRepository) {
        this.altKategoriRepository = altKategoriRepository;
    }

    /**
     * Tüm alt kategorileri getir
     */
    public List<AltKategori> getTumAltKategoriler() {
        return altKategoriRepository.findAll();
    }

    /**
     * ID'ye göre alt kategori getir
     */
    public AltKategori getAltKategoriById(Long id) {
        return altKategoriRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alt kategori bulunamadı"));
    }

    /**
     * Kategoriye göre alt kategorileri getir
     */
    public List<AltKategori> getAltKategorilerByKategori(Long kategoriId) {
        return altKategoriRepository.findByKategoriId(kategoriId);
    }

    /**
     * Alt kategori kaydet
     */
    @Transactional
    public AltKategori saveAltKategori(AltKategori altKategori) {
        return altKategoriRepository.save(altKategori);
    }

    /**
     * Yeni alt kategori oluştur
     */
    @Transactional
    public AltKategori createAltKategori(String ad, Kategori kategori, Sezon sezon) {
        AltKategori altKategori = new AltKategori(ad, kategori, sezon);
        return altKategoriRepository.save(altKategori);
    }

    /**
     * Alt kategori güncelle
     */
    @Transactional
    public AltKategori updateAltKategori(Long id, String yeniAd, Sezon yeniSezon) {
        AltKategori altKategori = getAltKategoriById(id);
        altKategori.setAd(yeniAd);
        altKategori.setSezon(yeniSezon);
        return altKategoriRepository.save(altKategori);
    }

    /**
     * Alt kategori sil
     */
    @Transactional
    public void deleteAltKategori(Long id) {
        altKategoriRepository.deleteById(id);
    }

    /**
     * Alt kategori sayısını getir
     */
    public long getAltKategoriSayisi() {
        return altKategoriRepository.count();
    }

    /**
     * Kategoriye göre alt kategori sayısını getir
     */
    public long getAltKategoriSayisiByKategori(Long kategoriId) {
        return altKategoriRepository.findByKategoriId(kategoriId).size();
    }
}
