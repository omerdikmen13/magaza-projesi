package com.magazaapp.controller.api;

import com.magazaapp.model.*;
import com.magazaapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sepet")
@CrossOrigin(origins = "*")
public class SepetRestController {

    @Autowired
    private SepetRepository sepetRepository;

    @Autowired
    private KullaniciRepository kullaniciRepository;

    @Autowired
    private UrunRepository urunRepository;

    @Autowired
    private BedenRepository bedenRepository;

    @Autowired
    private UrunStokRepository urunStokRepository;

    @Autowired
    private SiparisFisiRepository siparisFisiRepository;

    @Autowired
    private SiparisDetayRepository siparisDetayRepository;

    // =============== SEPETİ GETİR ===============
    @GetMapping
    public ResponseEntity<?> sepetGetir(@RequestHeader("Authorization") String token) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            List<Sepet> sepetListesi = sepetRepository.findByKullaniciId(kullanici.getId());

            BigDecimal toplam = sepetListesi.stream()
                    .map(item -> item.getUrun().getFiyat().multiply(BigDecimal.valueOf(item.getAdet())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<Map<String, Object>> sepetItems = sepetListesi.stream()
                    .map(this::createSepetResponse)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("sepetItems", sepetItems);
            response.put("toplam", toplam);
            response.put("itemSayisi", sepetListesi.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sepet getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== SEPETE EKLE ===============
    @PostMapping("/ekle")
    public ResponseEntity<?> sepeteEkle(@RequestHeader("Authorization") String token,
            @RequestBody SepetEkleRequest request) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            Urun urun = urunRepository.findById(request.getUrunId())
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            Beden beden = bedenRepository.findById(request.getBedenId())
                    .orElseThrow(() -> new RuntimeException("Beden bulunamadı"));

            // Stok kontrolü
            UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(request.getUrunId(), request.getBedenId())
                    .orElseThrow(() -> new RuntimeException("Stok bilgisi bulunamadı"));

            if (stok.getAdet() < request.getAdet()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Yetersiz stok! Mevcut: " + stok.getAdet()));
            }

            // Mevcut sepet kontrolü
            Optional<Sepet> mevcutSepet = sepetRepository.findByKullaniciIdAndUrunIdAndBedenId(
                    kullanici.getId(), request.getUrunId(), request.getBedenId());

            Sepet sepet;
            if (mevcutSepet.isPresent()) {
                sepet = mevcutSepet.get();
                int yeniAdet = sepet.getAdet() + request.getAdet();
                if (stok.getAdet() < yeniAdet) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Yetersiz stok! Mevcut: " + stok.getAdet()));
                }
                sepet.setAdet(yeniAdet);
            } else {
                sepet = new Sepet();
                sepet.setKullanici(kullanici);
                sepet.setUrun(urun);
                sepet.setBeden(beden);
                sepet.setAdet(request.getAdet());
            }

            sepetRepository.save(sepet);

            return ResponseEntity.ok(Map.of(
                    "message", "Ürün sepete eklendi",
                    "sepetItem", createSepetResponse(sepet)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sepete eklerken hata: " + e.getMessage()));
        }
    }

    // =============== SEPET GÜNCELLE ===============
    @PutMapping("/{id}")
    public ResponseEntity<?> sepetGuncelle(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody SepetGuncelleRequest request) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            Sepet sepet = sepetRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sepet öğesi bulunamadı"));

            // Yetki kontrolü
            if (!sepet.getKullanici().getId().equals(kullanici.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu sepet öğesi size ait değil"));
            }

            if (request.getAdet() <= 0) {
                sepetRepository.deleteById(id);
                return ResponseEntity.ok(Map.of("message", "Ürün sepetten silindi"));
            }

            // Stok kontrolü
            UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(
                    sepet.getUrun().getId(), sepet.getBeden().getId())
                    .orElseThrow(() -> new RuntimeException("Stok bilgisi bulunamadı"));

            if (stok.getAdet() < request.getAdet()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Yetersiz stok! Mevcut: " + stok.getAdet()));
            }

            sepet.setAdet(request.getAdet());
            sepetRepository.save(sepet);

            return ResponseEntity.ok(Map.of(
                    "message", "Sepet güncellendi",
                    "sepetItem", createSepetResponse(sepet)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sepet güncellerken hata: " + e.getMessage()));
        }
    }

    // =============== SEPETTEN SİL ===============
    @DeleteMapping("/{id}")
    public ResponseEntity<?> sepettenSil(@RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            Sepet sepet = sepetRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sepet öğesi bulunamadı"));

            if (!sepet.getKullanici().getId().equals(kullanici.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu sepet öğesi size ait değil"));
            }

            sepetRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Ürün sepetten silindi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sepetten silerken hata: " + e.getMessage()));
        }
    }

    // =============== SEPETİ BOŞALT ===============
    @DeleteMapping("/bosalt")
    @Transactional
    public ResponseEntity<?> sepetiBosalt(@RequestHeader("Authorization") String token) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            sepetRepository.deleteByKullaniciId(kullanici.getId());
            return ResponseEntity.ok(Map.of("message", "Sepet boşaltıldı"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sepet boşaltılırken hata: " + e.getMessage()));
        }
    }

    // =============== SİPARİŞ VER ===============
    @PostMapping("/siparis-ver")
    @Transactional
    public ResponseEntity<?> siparisVer(@RequestHeader("Authorization") String token,
            @RequestBody(required = false) SiparisVerRequest request) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            List<Sepet> sepetListesi = sepetRepository.findByKullaniciId(kullanici.getId());

            if (sepetListesi.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Sepetiniz boş"));
            }

            // Mağaza kontrolü
            Magaza magaza = sepetListesi.get(0).getUrun().getMagaza();
            for (Sepet sepetItem : sepetListesi) {
                if (!sepetItem.getUrun().getMagaza().getId().equals(magaza.getId())) {
                    return ResponseEntity.badRequest().body(Map.of("error",
                            "Sepetinizde farklı mağazalardan ürünler var. Tek mağazadan sipariş verin."));
                }
            }

            // Stok kontrolü
            for (Sepet sepetItem : sepetListesi) {
                UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(
                        sepetItem.getUrun().getId(), sepetItem.getBeden().getId())
                        .orElseThrow(() -> new RuntimeException("Stok bulunamadı"));

                if (stok.getAdet() < sepetItem.getAdet()) {
                    return ResponseEntity.badRequest().body(Map.of("error",
                            "Yetersiz stok: " + sepetItem.getUrun().getAd() + " - Mevcut: " + stok.getAdet()));
                }
            }

            // Toplam hesapla
            BigDecimal toplamTutar = sepetListesi.stream()
                    .map(item -> item.getUrun().getFiyat().multiply(BigDecimal.valueOf(item.getAdet())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Teslimat adresi
            String teslimatAdresi = (request != null && request.getTeslimatAdresi() != null)
                    ? request.getTeslimatAdresi()
                    : (kullanici.getAdres() != null ? kullanici.getAdres() : "Adres yok");

            // Sipariş oluştur
            SiparisFisi siparisFisi = new SiparisFisi();
            siparisFisi.setKullanici(kullanici);
            siparisFisi.setMagaza(magaza);
            siparisFisi.setToplamTutar(toplamTutar);
            siparisFisi.setTeslimatAdresi(teslimatAdresi);
            siparisFisi.setDurum(SiparisDurum.BEKLEMEDE);
            siparisFisi = siparisFisiRepository.save(siparisFisi);

            // Detayları oluştur ve stok düş
            for (Sepet sepetItem : sepetListesi) {
                SiparisDetay detay = new SiparisDetay();
                detay.setSiparisFisi(siparisFisi);
                detay.setUrun(sepetItem.getUrun());
                detay.setBeden(sepetItem.getBeden());
                detay.setAdet(sepetItem.getAdet());
                detay.setBirimFiyat(sepetItem.getUrun().getFiyat());
                detay.setToplamFiyat(sepetItem.getUrun().getFiyat().multiply(BigDecimal.valueOf(sepetItem.getAdet())));
                siparisDetayRepository.save(detay);

                // Stok düş
                UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(
                        sepetItem.getUrun().getId(), sepetItem.getBeden().getId()).get();
                stok.setAdet(stok.getAdet() - sepetItem.getAdet());
                urunStokRepository.save(stok);
            }

            // Sepeti temizle
            sepetRepository.deleteByKullaniciId(kullanici.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Sipariş başarıyla oluşturuldu",
                    "siparisId", siparisFisi.getId(),
                    "toplamTutar", toplamTutar));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sipariş oluşturulurken hata: " + e.getMessage()));
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

    private Map<String, Object> createSepetResponse(Sepet sepet) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", sepet.getId());
        map.put("adet", sepet.getAdet());
        map.put("urunId", sepet.getUrun().getId());
        map.put("urunAd", sepet.getUrun().getAd());
        map.put("urunFiyat", sepet.getUrun().getFiyat());
        map.put("urunResim", sepet.getUrun().getResimUrl());
        map.put("bedenId", sepet.getBeden().getId());
        map.put("bedenAd", sepet.getBeden().getAd());
        map.put("toplamFiyat", sepet.getUrun().getFiyat().multiply(BigDecimal.valueOf(sepet.getAdet())));
        return map;
    }

    // =============== REQUEST CLASSES ===============
    static class SepetEkleRequest {
        private Long urunId;
        private Long bedenId;
        private int adet = 1;

        public Long getUrunId() {
            return urunId;
        }

        public void setUrunId(Long urunId) {
            this.urunId = urunId;
        }

        public Long getBedenId() {
            return bedenId;
        }

        public void setBedenId(Long bedenId) {
            this.bedenId = bedenId;
        }

        public int getAdet() {
            return adet;
        }

        public void setAdet(int adet) {
            this.adet = adet;
        }
    }

    static class SepetGuncelleRequest {
        private int adet;

        public int getAdet() {
            return adet;
        }

        public void setAdet(int adet) {
            this.adet = adet;
        }
    }

    static class SiparisVerRequest {
        private String teslimatAdresi;

        public String getTeslimatAdresi() {
            return teslimatAdresi;
        }

        public void setTeslimatAdresi(String teslimatAdresi) {
            this.teslimatAdresi = teslimatAdresi;
        }
    }
}
