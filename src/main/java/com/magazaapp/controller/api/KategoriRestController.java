package com.magazaapp.controller.api;

import com.magazaapp.model.Kategori;
import com.magazaapp.model.AltKategori;
import com.magazaapp.repository.KategoriRepository;
import com.magazaapp.repository.AltKategoriRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/kategoriler")
@CrossOrigin(origins = "*")
public class KategoriRestController {

    @Autowired
    private KategoriRepository kategoriRepository;

    @Autowired
    private AltKategoriRepository altKategoriRepository;

    // =============== TÜM KATEGORİLER ===============
    @GetMapping
    public ResponseEntity<?> tumKategoriler() {
        try {
            List<Kategori> kategoriler = kategoriRepository.findAll();

            List<Map<String, Object>> response = kategoriler.stream()
                    .map(this::createKategoriResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Kategoriler getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== ALT KATEGORİLER ===============
    @GetMapping("/{kategoriId}/alt-kategoriler")
    public ResponseEntity<?> altKategoriler(@PathVariable Long kategoriId) {
        try {
            List<AltKategori> altKategoriler = altKategoriRepository.findByKategoriId(kategoriId);

            List<Map<String, Object>> response = altKategoriler.stream()
                    .map(this::createAltKategoriResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Alt kategoriler getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== HELPER METHODS ===============
    private Map<String, Object> createKategoriResponse(Kategori kategori) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", kategori.getId());
        map.put("ad", kategori.getAd());
        map.put("resimUrl", kategori.getResimUrl());
        return map;
    }

    private Map<String, Object> createAltKategoriResponse(AltKategori altKategori) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", altKategori.getId());
        map.put("ad", altKategori.getAd());
        map.put("sezon", altKategori.getSezon().toString());
        map.put("kategoriId", altKategori.getKategori().getId());
        return map;
    }
}
