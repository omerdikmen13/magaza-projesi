package com.magazaapp.controller.api;

import com.magazaapp.model.Favori;
import com.magazaapp.model.Kullanici;
import com.magazaapp.model.Urun;
import com.magazaapp.repository.FavoriRepository;
import com.magazaapp.repository.KullaniciRepository;
import com.magazaapp.repository.UrunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favoriler")
@CrossOrigin(origins = "*")
public class FavoriRestController {

    @Autowired
    private FavoriRepository favoriRepository;

    @Autowired
    private KullaniciRepository kullaniciRepository;

    @Autowired
    private UrunRepository urunRepository;

    // =============== TÜM FAVORİLERİ GETİR ===============
    @GetMapping
    public ResponseEntity<?> getFavoriler(@RequestHeader("Authorization") String token) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            List<Favori> favoriler = favoriRepository.findByKullaniciOrderByEklenmeTarihiDesc(kullanici);

            List<Map<String, Object>> response = favoriler.stream()
                    .map(favori -> {
                        Map<String, Object> map = new HashMap<>();
                        Urun urun = favori.getUrun();
                        map.put("id", favori.getId());
                        map.put("urunId", urun.getId());
                        map.put("urunAd", urun.getAd());
                        map.put("fiyat", urun.getFiyat());
                        map.put("resimUrl", urun.getResimUrl());
                        map.put("magazaAd", urun.getMagaza() != null ? urun.getMagaza().getAd() : "");
                        map.put("kategoriAd",
                                urun.getAltKategori() != null && urun.getAltKategori().getKategori() != null
                                        ? urun.getAltKategori().getKategori().getAd()
                                        : "");
                        map.put("eklenmeTarihi", favori.getEklenmeTarihi().toString());
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "favoriler", response,
                    "toplam", response.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Favoriler getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== FAVORİ ID'LERİNİ GETİR (Hızlı kontrol için) ===============
    @GetMapping("/ids")
    public ResponseEntity<?> getFavoriIds(@RequestHeader("Authorization") String token) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            List<Long> favoriIds = favoriRepository.findUrunIdsByKullanici(kullanici);
            return ResponseEntity.ok(Map.of("favoriIds", favoriIds));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =============== FAVORİYE EKLE ===============
    @PostMapping("/{urunId}")
    public ResponseEntity<?> favoriEkle(
            @RequestHeader("Authorization") String token,
            @PathVariable Long urunId) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            Optional<Urun> urunOpt = urunRepository.findById(urunId);
            if (urunOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ürün bulunamadı"));
            }

            Urun urun = urunOpt.get();

            // Zaten favorilerde mi kontrol et
            Optional<Favori> mevcutFavori = favoriRepository.findByKullaniciAndUrun(kullanici, urun);
            if (mevcutFavori.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "message", "Ürün zaten favorilerde",
                        "favoriId", mevcutFavori.get().getId()));
            }

            // Yeni favori oluştur
            Favori favori = new Favori(kullanici, urun);
            favoriRepository.save(favori);

            return ResponseEntity.ok(Map.of(
                    "message", "Ürün favorilere eklendi",
                    "favoriId", favori.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Favori eklenirken hata: " + e.getMessage()));
        }
    }

    // =============== FAVORİDEN KALDIR ===============
    @DeleteMapping("/{urunId}")
    @Transactional
    public ResponseEntity<?> favoriKaldir(
            @RequestHeader("Authorization") String token,
            @PathVariable Long urunId) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            Optional<Urun> urunOpt = urunRepository.findById(urunId);
            if (urunOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ürün bulunamadı"));
            }

            favoriRepository.deleteByKullaniciAndUrun(kullanici, urunOpt.get());

            return ResponseEntity.ok(Map.of("message", "Ürün favorilerden kaldırıldı"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Favori kaldırılırken hata: " + e.getMessage()));
        }
    }

    // =============== FAVORİ TOGGLE (Ekle/Kaldır) ===============
    @PostMapping("/toggle/{urunId}")
    @Transactional
    public ResponseEntity<?> favoriToggle(
            @RequestHeader("Authorization") String token,
            @PathVariable Long urunId) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            Optional<Urun> urunOpt = urunRepository.findById(urunId);
            if (urunOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ürün bulunamadı"));
            }

            Urun urun = urunOpt.get();
            Optional<Favori> mevcutFavori = favoriRepository.findByKullaniciAndUrun(kullanici, urun);

            if (mevcutFavori.isPresent()) {
                // Favoriden kaldır
                favoriRepository.delete(mevcutFavori.get());
                return ResponseEntity.ok(Map.of(
                        "message", "Favorilerden kaldırıldı",
                        "isFavorite", false));
            } else {
                // Favoriye ekle
                Favori favori = new Favori(kullanici, urun);
                favoriRepository.save(favori);
                return ResponseEntity.ok(Map.of(
                        "message", "Favorilere eklendi",
                        "isFavorite", true,
                        "favoriId", favori.getId()));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "İşlem sırasında hata: " + e.getMessage()));
        }
    }

    // =============== HELPER ===============
    private Kullanici getKullaniciFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        String kullaniciAdi = token.substring(7);
        return kullaniciRepository.findByKullaniciAdi(kullaniciAdi).orElse(null);
    }
}
