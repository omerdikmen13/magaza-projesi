package com.magazaapp.controller.api;

import com.magazaapp.model.Magaza;
import com.magazaapp.repository.MagazaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/magazalar")
@CrossOrigin(origins = "*")
public class MagazaRestController {

    @Autowired
    private MagazaRepository magazaRepository;

    // =============== TÜM MAĞAZALARI GETİR ===============
    @GetMapping
    public ResponseEntity<?> tumMagazalar() {
        try {
            List<Magaza> magazalar = magazaRepository.findByAktifTrue();

            List<Map<String, Object>> response = magazalar.stream()
                    .map(this::createMagazaResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mağazalar getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA DETAY ===============
    @GetMapping("/{id}")
    public ResponseEntity<?> magazaDetay(@PathVariable Long id) {
        try {
            return magazaRepository.findById(id)
                    .map(magaza -> ResponseEntity.ok(createMagazaResponse(magaza)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mağaza getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== HELPER METHODS ===============
    private Map<String, Object> createMagazaResponse(Magaza magaza) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", magaza.getId());
        map.put("ad", magaza.getAd());
        map.put("aciklama", magaza.getAciklama());
        map.put("logoUrl", magaza.getLogoUrl());
        map.put("aktif", magaza.getAktif());
        map.put("sahipId", magaza.getSahip().getId());
        map.put("olusturmaTarihi", magaza.getOlusturmaTarihi());
        return map;
    }
}
