package com.magazaapp.service;

import com.magazaapp.model.Favori;
import com.magazaapp.model.Kullanici;
import com.magazaapp.model.Urun;
import com.magazaapp.repository.FavoriRepository;
import com.magazaapp.repository.KullaniciRepository;
import com.magazaapp.repository.UrunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriService {

    private final FavoriRepository favoriRepository;
    private final KullaniciRepository kullaniciRepository;
    private final UrunRepository urunRepository;

    public FavoriService(FavoriRepository favoriRepository,
            KullaniciRepository kullaniciRepository,
            UrunRepository urunRepository) {
        this.favoriRepository = favoriRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.urunRepository = urunRepository;
    }

    /**
     * Favoriye ekle
     */
    @Transactional
    public void favoriyeEkle(Long kullaniciId, Long urunId) {
        // Önce zaten favori mi kontrol et
        if (favorideMi(kullaniciId, urunId)) {
            throw new RuntimeException("Bu ürün zaten favorilerinizde!");
        }

        Kullanici kullanici = kullaniciRepository.findById(kullaniciId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Urun urun = urunRepository.findById(urunId)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

        Favori favori = new Favori();
        favori.setKullanici(kullanici);
        favori.setUrun(urun);
        favoriRepository.save(favori);
    }

    /**
     * Favoriden çıkar
     */
    @Transactional
    public void favoridenCikar(Long kullaniciId, Long urunId) {
        Favori favori = favoriRepository.findByKullaniciIdAndUrunId(kullaniciId, urunId)
                .orElseThrow(() -> new RuntimeException("Favori bulunamadı"));
        favoriRepository.delete(favori);
    }

    /**
     * Kullanıcının favori ürünlerini getir
     */
    public List<Urun> getKullaniciFavorileri(Long kullaniciId) {
        List<Favori> favoriler = favoriRepository.findByKullaniciId(kullaniciId);
        return favoriler.stream()
                .map(Favori::getUrun)
                .collect(Collectors.toList());
    }

    /**
     * Ürün favoride mi kontrol et
     */
    public boolean favorideMi(Long kullaniciId, Long urunId) {
        return favoriRepository.findByKullaniciIdAndUrunId(kullaniciId, urunId).isPresent();
    }

    /**
     * Favorileri toggle (varsa çıkar, yoksa ekle)
     */
    @Transactional
    public boolean favoriToggle(Long kullaniciId, Long urunId) {
        if (favorideMi(kullaniciId, urunId)) {
            favoridenCikar(kullaniciId, urunId);
            return false; // Favoriden çıkarıldı
        } else {
            favoriyeEkle(kullaniciId, urunId);
            return true; // Favoriye eklendi
        }
    }

    /**
     * Kullanıcının favori sayısını getir
     */
    public long getFavoriSayisi(Long kullaniciId) {
        return favoriRepository.findByKullaniciId(kullaniciId).size();
    }

    /**
     * Toggle favori alias
     */
    @Transactional
    public boolean toggleFavori(Long kullaniciId, Long urunId) {
        return favoriToggle(kullaniciId, urunId);
    }

    /**
     * Remove favori alias
     */
    @Transactional
    public void removeFavori(Long kullaniciId, Long urunId) {
        favoridenCikar(kullaniciId, urunId);
    }

    /**
     * Get user favorites as Favori list (for controller)
     */
    public List<Favori> getKullaniciFavorileriList(Long kullaniciId) {
        return favoriRepository.findByKullaniciId(kullaniciId);
    }
}
