package com.magazaapp.controller.api;

import com.magazaapp.model.Urun;
import com.magazaapp.model.UrunStok;
import com.magazaapp.repository.UrunRepository;
import com.magazaapp.repository.UrunStokRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/urunler")
@CrossOrigin(origins = "*")
public class UrunRestController {

    @Autowired
    private UrunRepository urunRepository;

    @Autowired
    private UrunStokRepository urunStokRepository;

    // =============== MAĞAZANIN ÜRÜNLERİ ===============
    @GetMapping("/magaza/{magazaId}")
    public ResponseEntity<?> magazaUrunleri(
            @PathVariable Long magazaId,
            @RequestParam(required = false) Long kategoriId,
            @RequestParam(required = false) Long altKategoriId) {
        try {
            List<Urun> urunler = urunRepository.findByFiltre(magazaId, kategoriId, altKategoriId);

            List<Map<String, Object>> response = urunler.stream()
                    .map(this::createUrunResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ürünler getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== ÜRÜN DETAY ===============
    @GetMapping("/{id}")
    public ResponseEntity<?> urunDetay(@PathVariable Long id) {
        try {
            return urunRepository.findById(id)
                    .map(urun -> {
                        Map<String, Object> response = createUrunResponse(urun);

                        // Stok bilgilerini ekle
                        List<UrunStok> stoklar = urunStokRepository.findByUrunId(id);
                        List<Map<String, Object>> stokListesi = stoklar.stream()
                                .map(stok -> {
                                    Map<String, Object> stokMap = new HashMap<>();
                                    stokMap.put("bedenId", stok.getBeden().getId());
                                    stokMap.put("bedenAd", stok.getBeden().getAd());
                                    stokMap.put("adet", stok.getAdet());
                                    stokMap.put("stokta", stok.getAdet() > 0);
                                    return stokMap;
                                })
                                .collect(Collectors.toList());

                        response.put("stoklar", stokListesi);

                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ürün getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== TÜM AKTİF ÜRÜNLER ===============
    @GetMapping
    public ResponseEntity<?> tumUrunler() {
        try {
            List<Urun> urunler = urunRepository.findByAktifTrue();

            List<Map<String, Object>> response = urunler.stream()
                    .map(this::createUrunResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ürünler getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== ÜRÜN ARAMA ===============
    @GetMapping("/ara")
    public ResponseEntity<?> aramaYap(@RequestParam String q) {
        try {
            if (q == null || q.trim().length() < 2) {
                return ResponseEntity.badRequest().body(Map.of("error", "Arama terimi en az 2 karakter olmalıdır"));
            }

            String aramaKelimesi = q.trim().toLowerCase();
            List<Urun> tumUrunler = urunRepository.findByAktifTrue();

            // Ürün adı, açıklama, mağaza adı veya kategori ile eşleştir
            List<Map<String, Object>> sonuclar = tumUrunler.stream()
                    .filter(urun -> {
                        String ad = urun.getAd() != null ? urun.getAd().toLowerCase() : "";
                        String aciklama = urun.getAciklama() != null ? urun.getAciklama().toLowerCase() : "";
                        String magazaAd = urun.getMagaza() != null && urun.getMagaza().getAd() != null
                                ? urun.getMagaza().getAd().toLowerCase()
                                : "";
                        String kategori = urun.getAltKategori() != null && urun.getAltKategori().getKategori() != null
                                ? urun.getAltKategori().getKategori().getAd().toLowerCase()
                                : "";
                        String altKategori = urun.getAltKategori() != null
                                ? urun.getAltKategori().getAd().toLowerCase()
                                : "";
                        String renk = urun.getRenk() != null ? urun.getRenk().toLowerCase() : "";

                        return ad.contains(aramaKelimesi) ||
                                aciklama.contains(aramaKelimesi) ||
                                magazaAd.contains(aramaKelimesi) ||
                                kategori.contains(aramaKelimesi) ||
                                altKategori.contains(aramaKelimesi) ||
                                renk.contains(aramaKelimesi);
                    })
                    .map(this::createUrunResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "sonuclar", sonuclar,
                    "toplam", sonuclar.size(),
                    "aramaKelimesi", q));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Arama yapılırken hata: " + e.getMessage()));
        }
    }

    // =============== HELPER METHODS ===============
    private Map<String, Object> createUrunResponse(Urun urun) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", urun.getId());
        map.put("ad", urun.getAd());
        map.put("aciklama", urun.getAciklama());
        map.put("fiyat", urun.getFiyat());
        map.put("resimUrl", urun.getResimUrl());
        map.put("renk", urun.getRenk());
        map.put("aktif", urun.getAktif());
        map.put("magazaId", urun.getMagaza().getId());
        map.put("magazaAd", urun.getMagaza().getAd());
        map.put("kategoriId", urun.getAltKategori().getKategori().getId());
        map.put("altKategoriId", urun.getAltKategori().getId());
        map.put("altKategoriAd", urun.getAltKategori().getAd());
        map.put("kategoriAd", urun.getAltKategori().getKategori().getAd());
        map.put("sezon", urun.getAltKategori().getSezon().toString());
        return map;
    }
}
