package com.magazaapp.controller.api;

import com.magazaapp.model.*;
import com.magazaapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/siparisler")
@CrossOrigin(origins = "*")
public class SiparisRestController {

    @Autowired
    private SiparisFisiRepository siparisFisiRepository;

    @Autowired
    private SiparisDetayRepository siparisDetayRepository;

    @Autowired
    private KullaniciRepository kullaniciRepository;

    // =============== KULLANICI SİPARİŞLERİ ===============
    @GetMapping
    public ResponseEntity<?> siparislerim(@RequestHeader("Authorization") String token) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            List<SiparisFisi> siparisler = siparisFisiRepository.findByKullaniciId(kullanici.getId());

            List<Map<String, Object>> response = siparisler.stream()
                    .map(this::createSiparisResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Siparişler getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== SİPARİŞ DETAY ===============
    @GetMapping("/{id}")
    public ResponseEntity<?> siparisDetay(@RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            SiparisFisi siparis = siparisFisiRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

            // Yetki kontrolü
            if (!siparis.getKullanici().getId().equals(kullanici.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu siparişi görme yetkiniz yok"));
            }

            List<SiparisDetay> detaylar = siparisDetayRepository.findBySiparisFisiId(id);

            Map<String, Object> response = createSiparisResponse(siparis);

            List<Map<String, Object>> detayListesi = detaylar.stream()
                    .map(detay -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", detay.getId());
                        map.put("urunId", detay.getUrun().getId());
                        map.put("urunAd", detay.getUrun().getAd());
                        map.put("urunResim", detay.getUrun().getResimUrl());
                        map.put("bedenAd", detay.getBeden().getAd());
                        map.put("adet", detay.getAdet());
                        map.put("birimFiyat", detay.getBirimFiyat());
                        map.put("toplamFiyat", detay.getToplamFiyat());
                        return map;
                    })
                    .collect(Collectors.toList());

            response.put("detaylar", detayListesi);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Sipariş detayı getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== HELPER METHODS ===============
    private Kullanici getKullaniciFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        String tokenValue = token.substring(7);
        if (tokenValue.startsWith("simple-token-")) {
            try {
                Long userId = Long.parseLong(tokenValue.replace("simple-token-", ""));
                return kullaniciRepository.findById(userId).orElse(null);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Map<String, Object> createSiparisResponse(SiparisFisi siparis) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", siparis.getId());
        map.put("toplamTutar", siparis.getToplamTutar());
        map.put("durum", siparis.getDurum().toString());
        map.put("teslimatAdresi", siparis.getTeslimatAdresi());
        map.put("siparisTarihi", siparis.getSiparisTarihi());
        map.put("magazaId", siparis.getMagaza().getId());
        map.put("magazaAd", siparis.getMagaza().getAd());
        return map;
    }
}
