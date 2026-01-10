package com.magazaapp.controller.api;

import com.magazaapp.model.*;
import com.magazaapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mesajlar")
@CrossOrigin(origins = "*")
public class MesajRestController {

    @Autowired
    private MesajRepository mesajRepository;

    @Autowired
    private KullaniciRepository kullaniciRepository;

    @Autowired
    private MagazaRepository magazaRepository;

    // =============== MÜŞTERİ: SOHBET LİSTESİ ===============
    @GetMapping("/musteri/sohbetler")
    public ResponseEntity<?> musteriSohbetler(@RequestHeader("Authorization") String token) {
        try {
            Kullanici musteri = getKullaniciFromToken(token);
            if (musteri == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmalısınız"));
            }

            List<Mesaj> mesajlar = mesajRepository.findByMusteriIdOrderByTarihDesc(musteri.getId());

            // Mağaza bazında grupla (son mesaj)
            Map<Long, Mesaj> sonMesajlar = new LinkedHashMap<>();
            for (Mesaj m : mesajlar) {
                if (!sonMesajlar.containsKey(m.getMagaza().getId())) {
                    sonMesajlar.put(m.getMagaza().getId(), m);
                }
            }

            List<Map<String, Object>> response = sonMesajlar.values().stream()
                    .map(m -> {
                        Map<String, Object> sohbet = new HashMap<>();
                        sohbet.put("magazaId", m.getMagaza().getId());
                        sohbet.put("magazaAd", m.getMagaza().getAd());
                        sohbet.put("magazaLogo", m.getMagaza().getLogoUrl());
                        sohbet.put("sonMesaj", m.getIcerik());
                        sohbet.put("sonMesajTarih", m.getTarih());
                        sohbet.put("okundu", m.getOkundu());
                        return sohbet;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sohbetler getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== MÜŞTERİ: MAĞAZA İLE SOHBET ===============
    @GetMapping("/musteri/sohbet/{magazaId}")
    public ResponseEntity<?> musteriMagazaSohbet(@RequestHeader("Authorization") String token,
            @PathVariable Long magazaId) {
        try {
            Kullanici musteri = getKullaniciFromToken(token);
            if (musteri == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmalısınız"));
            }

            Magaza magaza = magazaRepository.findById(magazaId)
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            List<Mesaj> mesajlar = mesajRepository.findByMagazaIdAndMusteriIdOrderByTarihAsc(magazaId, musteri.getId());

            // Okundu işaretle
            for (Mesaj m : mesajlar) {
                if (!m.getGonderenMusteri()) {
                    m.setOkundu(true);
                    mesajRepository.save(m);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("magazaId", magaza.getId());
            response.put("magazaAd", magaza.getAd());
            response.put("mesajlar", mesajlar.stream()
                    .map(this::createMesajResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sohbet getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== MÜŞTERİ: MESAJ GÖNDER ===============
    @PostMapping("/musteri/gonder")
    public ResponseEntity<?> musteriMesajGonder(@RequestHeader("Authorization") String token,
            @RequestBody MesajRequest request) {
        try {
            Kullanici musteri = getKullaniciFromToken(token);
            if (musteri == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Giriş yapmalısınız"));
            }

            Magaza magaza = magazaRepository.findById(request.getMagazaId())
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            Mesaj mesaj = new Mesaj();
            mesaj.setGonderen(musteri);
            mesaj.setMusteri(musteri);
            mesaj.setMagaza(magaza);
            mesaj.setIcerik(request.getIcerik());
            mesaj.setGonderenMusteri(true);
            mesaj.setTarih(LocalDateTime.now());

            mesajRepository.save(mesaj);

            return ResponseEntity.ok(Map.of(
                    "message", "Mesaj gönderildi",
                    "mesaj", createMesajResponse(mesaj)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mesaj gönderilirken hata: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA SAHİBİ: MÜŞTERİ LİSTESİ ===============
    @GetMapping("/sahip/magaza/{magazaId}/musteriler")
    public ResponseEntity<?> sahipMusteriler(@RequestHeader("Authorization") String token,
            @PathVariable Long magazaId) {
        try {
            Kullanici sahip = getSahibiFromToken(token);
            if (sahip == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Mağaza sahibi yetkisi gerekli"));
            }

            Magaza magaza = magazaRepository.findById(magazaId)
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            if (!magaza.getSahip().getId().equals(sahip.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu mağaza size ait değil"));
            }

            List<Kullanici> musteriler = mesajRepository.findDistinctMusterilerByMagazaId(magazaId);

            List<Map<String, Object>> response = musteriler.stream()
                    .map(m -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", m.getId());
                        item.put("kullaniciAdi", m.getKullaniciAdi());
                        item.put("ad", m.getAd());
                        item.put("soyad", m.getSoyad());
                        return item;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Müşteriler getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA SAHİBİ: MÜŞTERİ İLE SOHBET ===============
    @GetMapping("/sahip/magaza/{magazaId}/musteri/{musteriId}")
    public ResponseEntity<?> sahipMusteriSohbet(@RequestHeader("Authorization") String token,
            @PathVariable Long magazaId, @PathVariable Long musteriId) {
        try {
            Kullanici sahip = getSahibiFromToken(token);
            if (sahip == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Mağaza sahibi yetkisi gerekli"));
            }

            Magaza magaza = magazaRepository.findById(magazaId)
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            if (!magaza.getSahip().getId().equals(sahip.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu mağaza size ait değil"));
            }

            Kullanici musteri = kullaniciRepository.findById(musteriId)
                    .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı"));

            List<Mesaj> mesajlar = mesajRepository.findByMagazaIdAndMusteriIdOrderByTarihAsc(magazaId, musteriId);

            // Okundu işaretle (müşteriden gelenler)
            for (Mesaj m : mesajlar) {
                if (m.getGonderenMusteri()) {
                    m.setOkundu(true);
                    mesajRepository.save(m);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("musteriId", musteri.getId());
            response.put("musteriAd", musteri.getAd() + " " + musteri.getSoyad());
            response.put("musteriKullaniciAdi", musteri.getKullaniciAdi());
            response.put("mesajlar", mesajlar.stream()
                    .map(this::createMesajResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sohbet getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA SAHİBİ: MESAJ GÖNDER ===============
    @PostMapping("/sahip/gonder")
    public ResponseEntity<?> sahipMesajGonder(@RequestHeader("Authorization") String token,
            @RequestBody SahipMesajRequest request) {
        try {
            Kullanici sahip = getSahibiFromToken(token);
            if (sahip == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Mağaza sahibi yetkisi gerekli"));
            }

            Magaza magaza = magazaRepository.findById(request.getMagazaId())
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            if (!magaza.getSahip().getId().equals(sahip.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu mağaza size ait değil"));
            }

            Kullanici musteri = kullaniciRepository.findById(request.getMusteriId())
                    .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı"));

            Mesaj mesaj = new Mesaj();
            mesaj.setGonderen(sahip);
            mesaj.setMusteri(musteri);
            mesaj.setMagaza(magaza);
            mesaj.setIcerik(request.getIcerik());
            mesaj.setGonderenMusteri(false);
            mesaj.setTarih(LocalDateTime.now());

            mesajRepository.save(mesaj);

            return ResponseEntity.ok(Map.of(
                    "message", "Mesaj gönderildi",
                    "mesaj", createMesajResponse(mesaj)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mesaj gönderilirken hata: " + e.getMessage()));
        }
    }

    // =============== ADMIN: TÜM MESAJLAR ===============
    @GetMapping("/admin/tumMesajlar")
    public ResponseEntity<?> adminTumMesajlar(@RequestHeader("Authorization") String token) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            List<Mesaj> mesajlar = mesajRepository.findAllByOrderByTarihDesc();

            // Mağaza ve müşteri bazında grupla
            Map<String, Map<String, Object>> sohbetler = new LinkedHashMap<>();
            for (Mesaj m : mesajlar) {
                if (m.getMagaza() == null || m.getMusteri() == null) {
                    continue; // Bozuk veriyi atla
                }
                String key = m.getMagaza().getId() + "-" + m.getMusteri().getId();
                if (!sohbetler.containsKey(key)) {
                    Map<String, Object> sohbet = new HashMap<>();
                    sohbet.put("magazaId", m.getMagaza().getId());
                    sohbet.put("magazaAd", m.getMagaza().getAd());
                    sohbet.put("musteriId", m.getMusteri().getId());
                    sohbet.put("musteriAd", m.getMusteri().getAd() + " " + m.getMusteri().getSoyad());
                    sohbet.put("musteriKullaniciAdi", m.getMusteri().getKullaniciAdi());
                    sohbet.put("sonMesaj", m.getIcerik());
                    sohbet.put("sonMesajTarih", m.getTarih());
                    sohbet.put("mesajSayisi", 1);
                    sohbetler.put(key, sohbet);
                } else {
                    Map<String, Object> existing = sohbetler.get(key);
                    existing.put("mesajSayisi", (Integer) existing.get("mesajSayisi") + 1);
                }
            }

            return ResponseEntity.ok(new ArrayList<>(sohbetler.values()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mesajlar getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== ADMIN: SOHBET DETAY (İzle/Yönet) ===============
    @GetMapping("/admin/sohbet/{magazaId}/{musteriId}")
    public ResponseEntity<?> adminSohbetDetay(@RequestHeader("Authorization") String token,
            @PathVariable Long magazaId, @PathVariable Long musteriId) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Magaza magaza = magazaRepository.findById(magazaId)
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            Kullanici musteri = kullaniciRepository.findById(musteriId)
                    .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı"));

            List<Mesaj> mesajlar = mesajRepository.findByMagazaIdAndMusteriIdOrderByTarihAsc(magazaId, musteriId);

            Map<String, Object> response = new HashMap<>();
            response.put("magazaId", magaza.getId());
            response.put("magazaAd", magaza.getAd());
            response.put("musteriId", musteri.getId());
            response.put("musteriAd", musteri.getAd() + " " + musteri.getSoyad());
            response.put("musteriKullaniciAdi", musteri.getKullaniciAdi());
            response.put("mesajlar", mesajlar.stream()
                    .map(this::createMesajResponse)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sohbet getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== ADMIN: MESAJ SİL ===============
    @DeleteMapping("/admin/mesaj/{mesajId}")
    public ResponseEntity<?> adminMesajSil(@RequestHeader("Authorization") String token,
            @PathVariable Long mesajId) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            if (!mesajRepository.existsById(mesajId)) {
                throw new RuntimeException("Mesaj bulunamadı");
            }

            mesajRepository.deleteById(mesajId);
            return ResponseEntity.ok(Map.of("message", "Mesaj silindi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Silme hatası: " + e.getMessage()));
        }
    }

    // =============== ADMIN: MESAJ GÖNDER (ARAYA GİRME) ===============
    @PostMapping("/admin/sohbet/gonder")
    public ResponseEntity<?> adminMesajGonder(@RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> payload) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Long magazaId = Long.valueOf(payload.get("magazaId").toString());
            Long musteriId = Long.valueOf(payload.get("musteriId").toString());
            String icerik = (String) payload.get("icerik");
            String yon = (String) payload.get("yon"); // "MAGAZA_TO_MUSTERI" veya "MUSTERI_TO_MAGAZA"

            Magaza magaza = magazaRepository.findById(magazaId)
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            Kullanici musteri = kullaniciRepository.findById(musteriId)
                    .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı"));

            Mesaj mesaj = new Mesaj();
            mesaj.setMagaza(magaza);
            mesaj.setMusteri(musteri);
            mesaj.setIcerik(icerik);
            mesaj.setTarih(LocalDateTime.now());
            mesaj.setOkundu(false);

            if ("MAGAZA_TO_MUSTERI".equals(yon)) {
                mesaj.setGonderen(magaza.getSahip());
                mesaj.setGonderenMusteri(false);
            } else {
                mesaj.setGonderen(musteri);
                mesaj.setGonderenMusteri(true);
            }

            mesajRepository.save(mesaj);

            return ResponseEntity.ok(Map.of("message", "Mesaj gönderildi", "mesaj", mesaj));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Gönderme hatası: " + e.getMessage()));
        }
    }

    // =============== ADMIN: MESAJ DÜZENLE ===============
    @PutMapping("/admin/mesaj/{id}")
    public ResponseEntity<?> adminMesajDuzenle(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Mesaj mesaj = mesajRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mesaj bulunamadı"));

            if (payload.containsKey("icerik")) {
                mesaj.setIcerik(payload.get("icerik"));
            }

            mesajRepository.save(mesaj);

            return ResponseEntity.ok(Map.of("message", "Mesaj güncellendi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Güncelleme hatası: " + e.getMessage()));
        }
    }

    private Kullanici getAdminFromToken(String token) {
        Kullanici kullanici = getKullaniciFromToken(token);
        if (kullanici != null && kullanici.getRol() == KullaniciRol.ADMIN) {
            return kullanici;
        }
        return null;
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

    private Kullanici getSahibiFromToken(String token) {
        Kullanici kullanici = getKullaniciFromToken(token);
        if (kullanici != null &&
                (kullanici.getRol() == KullaniciRol.MAGAZA_SAHIBI || kullanici.getRol() == KullaniciRol.ADMIN)) {
            return kullanici;
        }
        return null;
    }

    private Map<String, Object> createMesajResponse(Mesaj m) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", m.getId());
        map.put("icerik", m.getIcerik());
        map.put("tarih", m.getTarih());
        map.put("okundu", m.getOkundu());
        map.put("gonderenMusteri", m.getGonderenMusteri());
        map.put("gonderenAd", m.getGonderen().getAd());
        return map;
    }

    // =============== REQUEST CLASSES ===============
    static class MesajRequest {
        private Long magazaId;
        private String icerik;

        public Long getMagazaId() {
            return magazaId;
        }

        public void setMagazaId(Long magazaId) {
            this.magazaId = magazaId;
        }

        public String getIcerik() {
            return icerik;
        }

        public void setIcerik(String icerik) {
            this.icerik = icerik;
        }
    }

    static class SahipMesajRequest {
        private Long magazaId;
        private Long musteriId;
        private String icerik;

        public Long getMagazaId() {
            return magazaId;
        }

        public void setMagazaId(Long magazaId) {
            this.magazaId = magazaId;
        }

        public Long getMusteriId() {
            return musteriId;
        }

        public void setMusteriId(Long musteriId) {
            this.musteriId = musteriId;
        }

        public String getIcerik() {
            return icerik;
        }

        public void setIcerik(String icerik) {
            this.icerik = icerik;
        }
    }
}
