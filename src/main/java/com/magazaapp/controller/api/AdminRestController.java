package com.magazaapp.controller.api;

import com.magazaapp.model.*;
import com.magazaapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminRestController {

    @Autowired
    private KullaniciRepository kullaniciRepository;

    @Autowired
    private MagazaRepository magazaRepository;

    @Autowired
    private UrunRepository urunRepository;

    @Autowired
    private SiparisFisiRepository siparisFisiRepository;

    @Autowired
    private SiparisDetayRepository siparisDetayRepository;

    @Autowired
    private UrunStokRepository urunStokRepository;

    @Autowired
    private AltKategoriRepository altKategoriRepository;

    @Autowired
    private BedenRepository bedenRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // =============== DASHBOARD ===============
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(@RequestHeader("Authorization") String token) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            long toplamKullanici = kullaniciRepository.count();
            long toplamMagaza = magazaRepository.count();
            long toplamUrun = urunRepository.count();
            long toplamSiparis = siparisFisiRepository.count();

            long bekleyenSiparis = siparisFisiRepository.findAll().stream()
                    .filter(s -> s.getDurum() == SiparisDurum.BEKLEMEDE).count();

            BigDecimal toplamCiro = siparisFisiRepository.findAll().stream()
                    .filter(s -> s.getDurum() != SiparisDurum.IPTAL)
                    .map(s -> s.getToplamTutar() != null ? s.getToplamTutar() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Son 5 sipariş
            List<Map<String, Object>> sonSiparisler = siparisFisiRepository.findAll().stream()
                    .sorted((a, b) -> b.getSiparisTarihi().compareTo(a.getSiparisTarihi()))
                    .limit(5)
                    .map(this::createSiparisOzet)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("toplamKullanici", toplamKullanici);
            response.put("toplamMagaza", toplamMagaza);
            response.put("toplamUrun", toplamUrun);
            response.put("toplamSiparis", toplamSiparis);
            response.put("bekleyenSiparis", bekleyenSiparis);
            response.put("toplamCiro", toplamCiro);
            response.put("sonSiparisler", sonSiparisler);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Dashboard getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== KULLANICI LİSTESİ ===============
    @GetMapping("/kullanicilar")
    public ResponseEntity<?> kullaniciListesi(@RequestHeader("Authorization") String token) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            List<Map<String, Object>> kullanicilar = kullaniciRepository.findAll().stream()
                    .map(this::createKullaniciResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(kullanicilar);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Kullanıcılar getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== ROL DEĞİŞTİR ===============
    @PutMapping("/kullanicilar/{id}/rol")
    public ResponseEntity<?> rolDegistir(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Kullanici kullanici = kullaniciRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            String yeniRol = request.get("rol");
            KullaniciRol rol = KullaniciRol.valueOf(yeniRol);
            kullanici.setRol(rol);
            kullaniciRepository.save(kullanici);

            return ResponseEntity.ok(Map.of(
                    "message", "Rol güncellendi",
                    "kullanici", createKullaniciResponse(kullanici)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rol değiştirilirken hata: " + e.getMessage()));
        }
    }

    // =============== KULLANICI AKTİF/PASİF ===============
    @PutMapping("/kullanicilar/{id}/durum")
    public ResponseEntity<?> kullaniciDurumDegistir(@RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Kullanici kullanici = kullaniciRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            kullanici.setAktif(!kullanici.getAktif());
            kullaniciRepository.save(kullanici);

            return ResponseEntity.ok(Map.of(
                    "message", kullanici.getAktif() ? "Kullanıcı aktifleştirildi" : "Kullanıcı pasifleştirildi",
                    "kullanici", createKullaniciResponse(kullanici)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Durum değiştirilirken hata: " + e.getMessage()));
        }
    }

    // =============== KULLANICI EKLE ===============
    @PostMapping("/kullanicilar")
    public ResponseEntity<?> kullaniciEkle(@RequestHeader("Authorization") String token,
            @RequestBody KullaniciEkleRequest request) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            if (kullaniciRepository.existsByKullaniciAdi(request.getKullaniciAdi())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bu kullanıcı adı zaten kullanılıyor"));
            }
            if (request.getEmail() != null && kullaniciRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bu email zaten kullanılıyor"));
            }

            Kullanici kullanici = new Kullanici();
            kullanici.setKullaniciAdi(request.getKullaniciAdi());
            kullanici.setEmail(request.getEmail());
            kullanici.setSifre(passwordEncoder.encode(request.getSifre()));
            kullanici.setAd(request.getAd());
            kullanici.setSoyad(request.getSoyad());
            kullanici.setTelefon(request.getTelefon());
            kullanici.setAdres(request.getAdres());

            try {
                kullanici.setRol(KullaniciRol.valueOf(request.getRol()));
            } catch (Exception e) {
                kullanici.setRol(KullaniciRol.MUSTERI);
            }
            kullanici.setAktif(true);

            kullaniciRepository.save(kullanici);

            return ResponseEntity.ok(Map.of(
                    "message", "Kullanıcı oluşturuldu",
                    "kullanici", createKullaniciResponse(kullanici)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Kullanıcı oluşturulurken hata: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA LİSTESİ ===============
    @GetMapping("/magazalar")
    public ResponseEntity<?> magazaListesi(@RequestHeader("Authorization") String token) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            List<Map<String, Object>> magazalar = magazaRepository.findAll().stream()
                    .map(this::createMagazaResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(magazalar);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mağazalar getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA AKTİF/PASİF ===============
    @PutMapping("/magazalar/{id}/durum")
    public ResponseEntity<?> magazaDurumDegistir(@RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Magaza magaza = magazaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            magaza.setAktif(!magaza.getAktif());
            magazaRepository.save(magaza);

            return ResponseEntity.ok(Map.of(
                    "message", magaza.getAktif() ? "Mağaza aktifleştirildi" : "Mağaza pasifleştirildi",
                    "magaza", createMagazaResponse(magaza)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Durum değiştirilirken hata: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA EKLE ===============
    @PostMapping("/magazalar")
    public ResponseEntity<?> magazaEkle(@RequestHeader("Authorization") String token,
            @RequestBody MagazaEkleRequest request) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Magaza magaza = new Magaza();
            magaza.setAd(request.getAd());
            magaza.setAciklama(request.getAciklama());
            magaza.setLogoUrl(request.getLogoUrl());
            magaza.setAktif(true);

            if (request.getSahipId() != null) {
                Kullanici sahip = kullaniciRepository.findById(request.getSahipId()).orElse(null);
                if (sahip != null) {
                    magaza.setSahip(sahip);
                }
            }

            magazaRepository.save(magaza);

            return ResponseEntity.ok(Map.of(
                    "message", "Mağaza oluşturuldu",
                    "magaza", createMagazaResponse(magaza)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mağaza oluşturulurken hata: " + e.getMessage()));
        }
    }

    // =============== KULLANICI SİL ===============
    @DeleteMapping("/kullanicilar/{id}")
    public ResponseEntity<?> kullaniciSil(@RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Kullanici kullanici = kullaniciRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            if (kullanici.getRol() == KullaniciRol.ADMIN) {
                return ResponseEntity.badRequest().body(Map.of("error", "Admin kullanıcıları silinemez"));
            }

            kullaniciRepository.delete(kullanici);

            return ResponseEntity.ok(Map.of("message", "Kullanıcı silindi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Silme hatası: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA SİL ===============
    @DeleteMapping("/magazalar/{id}")
    public ResponseEntity<?> magazaSil(@RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Magaza magaza = magazaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            magazaRepository.delete(magaza);

            return ResponseEntity.ok(Map.of("message", "Mağaza silindi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Silme hatası: " + e.getMessage()));
        }
    }

    // =============== KULLANICI DÜZENLE ===============
    @PutMapping("/kullanicilar/{id}")
    public ResponseEntity<?> kullaniciDuzenle(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Kullanici kullanici = kullaniciRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            if (request.containsKey("ad"))
                kullanici.setAd(request.get("ad"));
            if (request.containsKey("soyad"))
                kullanici.setSoyad(request.get("soyad"));
            if (request.containsKey("email"))
                kullanici.setEmail(request.get("email"));
            if (request.containsKey("telefon"))
                kullanici.setTelefon(request.get("telefon"));

            kullaniciRepository.save(kullanici);

            return ResponseEntity.ok(Map.of(
                    "message", "Kullanıcı güncellendi",
                    "kullanici", createKullaniciResponse(kullanici)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Güncelleme hatası: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA DÜZENLE ===============
    @PutMapping("/magazalar/{id}")
    public ResponseEntity<?> magazaDuzenle(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Magaza magaza = magazaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            if (request.containsKey("ad"))
                magaza.setAd(request.get("ad"));
            if (request.containsKey("aciklama"))
                magaza.setAciklama(request.get("aciklama"));
            if (request.containsKey("logoUrl"))
                magaza.setLogoUrl(request.get("logoUrl"));

            magazaRepository.save(magaza);

            return ResponseEntity.ok(Map.of(
                    "message", "Mağaza güncellendi",
                    "magaza", createMagazaResponse(magaza)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Güncelleme hatası: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA ÜRÜNLERİ ===============
    @GetMapping("/magazalar/{id}/urunler")
    public ResponseEntity<?> magazaUrunleri(@RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Magaza magaza = magazaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            List<Map<String, Object>> urunler = urunRepository.findByMagazaId(id).stream()
                    .map(u -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", u.getId());
                        map.put("ad", u.getAd());
                        map.put("aciklama", u.getAciklama());
                        map.put("fiyat", u.getFiyat());
                        map.put("renk", u.getRenk());
                        map.put("resimUrl", u.getResimUrl());
                        map.put("aktif", u.getAktif());
                        map.put("kategori", u.getAltKategori() != null ? u.getAltKategori().getAd() : null);
                        return map;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("magaza", createMagazaResponse(magaza));
            response.put("urunler", urunler);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ürünler getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA ÜRÜN EKLE (ADMIN) ===============
    @PostMapping("/magazalar/{id}/urun")
    public ResponseEntity<?> addStoreProduct(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody UrunEkleRequest request) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Magaza magaza = magazaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            // Alt kategori
            AltKategori altKategori;
            if (request.getAltKategoriId() != null) {
                altKategori = altKategoriRepository.findById(request.getAltKategoriId())
                        .orElseThrow(() -> new RuntimeException("Alt kategori bulunamadı"));
            } else {
                altKategori = altKategoriRepository.findAll().stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Hiç alt kategori bulunamadı"));
            }

            Urun urun = new Urun();
            urun.setAd(request.getAd());
            urun.setAciklama(request.getAciklama());
            urun.setFiyat(request.getFiyat());
            urun.setRenk(request.getRenk());
            urun.setResimUrl(request.getResimUrl());
            urun.setMagaza(magaza);
            urun.setAltKategori(altKategori);
            urun.setAktif(true);
            urun = urunRepository.save(urun);

            // Stokları ekle
            if (request.getStoklar() != null) {
                for (StokRequest stokReq : request.getStoklar()) {
                    if (stokReq.getAdet() > 0) {
                        Beden beden = bedenRepository.findById(stokReq.getBedenId())
                                .orElseThrow(() -> new RuntimeException("Beden bulunamadı"));
                        UrunStok stok = new UrunStok();
                        stok.setUrun(urun);
                        stok.setBeden(beden);
                        stok.setAdet(stokReq.getAdet());
                        urunStokRepository.save(stok);
                    }
                }
            }

            // Basit response
            Map<String, Object> map = new HashMap<>();
            map.put("id", urun.getId());
            map.put("ad", urun.getAd());
            map.put("aktif", urun.getAktif());

            return ResponseEntity.ok(Map.of(
                    "message", "Ürün başarıyla eklendi",
                    "urun", map));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ürün eklenirken hata: " + e.getMessage()));
        }
    }

    // =============== ÜRÜN AKTİF/PASİF ===============
    @PutMapping("/urunler/{id}/durum")
    public ResponseEntity<?> urunDurumDegistir(@RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Urun urun = urunRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            urun.setAktif(!urun.getAktif());
            urunRepository.save(urun);

            return ResponseEntity.ok(Map.of(
                    "message", urun.getAktif() ? "Ürün aktifleştirildi" : "Ürün pasifleştirildi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Durum değiştirilirken hata: " + e.getMessage()));
        }
    }

    // =============== ÜRÜN SİL ===============
    @DeleteMapping("/urunler/{id}")
    public ResponseEntity<?> urunSil(@RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Urun urun = urunRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            urunRepository.delete(urun);

            return ResponseEntity.ok(Map.of("message", "Ürün silindi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Silme hatası: " + e.getMessage()));
        }
    }

    // =============== SİPARİŞ LİSTESİ ===============
    @GetMapping("/siparisler")
    public ResponseEntity<?> siparisListesi(@RequestHeader("Authorization") String token,
            @RequestParam(required = false) String durum) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            List<SiparisFisi> siparisler;
            if (durum != null && !durum.isEmpty()) {
                SiparisDurum siparisDurum = SiparisDurum.valueOf(durum);
                siparisler = siparisFisiRepository.findAll().stream()
                        .filter(s -> s.getDurum() == siparisDurum)
                        .sorted((a, b) -> b.getSiparisTarihi().compareTo(a.getSiparisTarihi()))
                        .collect(Collectors.toList());
            } else {
                siparisler = siparisFisiRepository.findAll().stream()
                        .sorted((a, b) -> b.getSiparisTarihi().compareTo(a.getSiparisTarihi()))
                        .collect(Collectors.toList());
            }

            List<Map<String, Object>> response = siparisler.stream()
                    .map(this::createSiparisResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Siparişler getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== SİPARİŞ DURUM GÜNCELLE ===============
    @PutMapping("/siparisler/{id}/durum")
    public ResponseEntity<?> siparisDurumGuncelle(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            SiparisFisi siparis = siparisFisiRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

            String yeniDurum = request.get("durum");
            SiparisDurum durum = SiparisDurum.valueOf(yeniDurum);
            siparis.setDurum(durum);
            siparisFisiRepository.save(siparis);

            return ResponseEntity.ok(Map.of(
                    "message", "Sipariş durumu güncellendi",
                    "siparis", createSiparisResponse(siparis)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Durum güncellenirken hata: " + e.getMessage()));
        }
    }

    // =============== CİRO RAPORU ===============
    @GetMapping("/ciro")
    public ResponseEntity<?> ciroRaporu(@RequestHeader("Authorization") String token,
            @RequestParam(required = false, defaultValue = "TUMU") String donem) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            LocalDateTime baslangicTarihi;
            LocalDateTime simdi = LocalDateTime.now();

            switch (donem) {
                case "HAFTALIK":
                    baslangicTarihi = simdi.minusWeeks(1);
                    break;
                case "AYLIK":
                    baslangicTarihi = simdi.minusMonths(1);
                    break;
                case "YILLIK":
                    baslangicTarihi = simdi.minusYears(1);
                    break;
                default:
                    baslangicTarihi = LocalDateTime.of(2000, 1, 1, 0, 0);
            }

            List<SiparisFisi> tumSiparisler = siparisFisiRepository.findAll().stream()
                    .filter(s -> s.getSiparisTarihi().isAfter(baslangicTarihi))
                    .collect(Collectors.toList());

            BigDecimal toplamCiro = tumSiparisler.stream()
                    .filter(s -> s.getDurum() != SiparisDurum.IPTAL)
                    .map(s -> s.getToplamTutar() != null ? s.getToplamTutar() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal tamamlananCiro = tumSiparisler.stream()
                    .filter(s -> s.getDurum() == SiparisDurum.TESLIM_EDILDI)
                    .map(s -> s.getToplamTutar() != null ? s.getToplamTutar() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal bekleyenCiro = tumSiparisler.stream()
                    .filter(s -> s.getDurum() == SiparisDurum.BEKLEMEDE)
                    .map(s -> s.getToplamTutar() != null ? s.getToplamTutar() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Mağaza bazlı raporlar
            List<Map<String, Object>> magazaRaporlari = magazaRepository.findAll().stream()
                    .map(magaza -> {
                        List<SiparisFisi> magazaSiparisleri = tumSiparisler.stream()
                                .filter(s -> s.getMagaza().getId().equals(magaza.getId()))
                                .collect(Collectors.toList());

                        BigDecimal magazaCiro = magazaSiparisleri.stream()
                                .filter(s -> s.getDurum() != SiparisDurum.IPTAL)
                                .map(s -> s.getToplamTutar() != null ? s.getToplamTutar() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        Map<String, Object> rapor = new HashMap<>();
                        rapor.put("magazaId", magaza.getId());
                        rapor.put("magazaAd", magaza.getAd());
                        rapor.put("siparisSayisi", magazaSiparisleri.size());
                        rapor.put("ciro", magazaCiro);
                        return rapor;
                    })
                    .sorted((r1, r2) -> ((BigDecimal) r2.get("ciro")).compareTo((BigDecimal) r1.get("ciro")))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("donem", donem);
            response.put("toplamCiro", toplamCiro);
            response.put("tamamlananCiro", tamamlananCiro);
            response.put("bekleyenCiro", bekleyenCiro);
            response.put("magazaRaporlari", magazaRaporlari);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Ciro raporu getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== SİPARİŞ DETAY ===============
    @GetMapping("/siparis/{id}")
    public ResponseEntity<?> getSiparis(@RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            SiparisFisi siparis = siparisFisiRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

            List<SiparisDetay> detaylar = siparisDetayRepository.findBySiparisFisiId(id);

            Map<String, Object> response = new HashMap<>();
            response.put("id", siparis.getId());
            response.put("durum", siparis.getDurum().toString());
            response.put("toplamTutar", siparis.getToplamTutar());
            response.put("teslimatAdresi", siparis.getTeslimatAdresi());
            response.put("siparisTarihi", siparis.getSiparisTarihi());
            response.put("kullaniciAd", siparis.getKullanici().getAd() + " " + siparis.getKullanici().getSoyad());
            response.put("magazaAd", siparis.getMagaza().getAd());

            List<Map<String, Object>> detayList = detaylar.stream().map(d -> {
                Map<String, Object> dm = new HashMap<>();
                dm.put("id", d.getId());
                dm.put("urunAd", d.getUrun().getAd());
                dm.put("bedenAd", d.getBeden() != null ? d.getBeden().getAd() : "Standart");
                dm.put("adet", d.getAdet());
                dm.put("birimFiyat", d.getBirimFiyat());
                dm.put("toplamFiyat", d.getToplamFiyat());
                return dm;
            }).collect(Collectors.toList());

            response.put("detaylar", detayList);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sipariş getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== SİPARİŞ İÇERİK GÜNCELLE ===============
    @PostMapping("/siparis/{id}/icerik-guncelle")
    public ResponseEntity<?> updateSiparisIcerik(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestParam List<Long> detayIds,
            @RequestParam List<Integer> adetler,
            @RequestParam(required = false) List<Long> silinecekIds) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            SiparisFisi siparis = siparisFisiRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

            // Silme işlemleri
            if (silinecekIds != null && !silinecekIds.isEmpty()) {
                for (Long detayId : silinecekIds) {
                    SiparisDetay detay = siparisDetayRepository.findById(detayId).orElse(null);
                    if (detay != null) {
                        // Stok iade
                        UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(
                                detay.getUrun().getId(), detay.getBeden() != null ? detay.getBeden().getId() : null)
                                .orElse(null);
                        if (stok != null) {
                            stok.setAdet(stok.getAdet() + detay.getAdet());
                            urunStokRepository.save(stok);
                        }
                        siparisDetayRepository.delete(detay);
                    }
                }
            }

            // Adet güncelleme
            java.math.BigDecimal yeniToplam = java.math.BigDecimal.ZERO;
            for (int i = 0; i < detayIds.size(); i++) {
                Long detayId = detayIds.get(i);
                Integer yeniAdet = adetler.get(i);

                SiparisDetay detay = siparisDetayRepository.findById(detayId).orElse(null);
                if (detay != null) {
                    int eskiAdet = detay.getAdet();
                    int fark = eskiAdet - yeniAdet;

                    // Stok ayarla
                    if (fark != 0) {
                        UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(
                                detay.getUrun().getId(), detay.getBeden() != null ? detay.getBeden().getId() : null)
                                .orElse(null);
                        if (stok != null) {
                            stok.setAdet(stok.getAdet() + fark);
                            urunStokRepository.save(stok);
                        }
                    }

                    detay.setAdet(yeniAdet);
                    detay.setToplamFiyat(detay.getBirimFiyat().multiply(java.math.BigDecimal.valueOf(yeniAdet)));
                    siparisDetayRepository.save(detay);

                    yeniToplam = yeniToplam.add(detay.getToplamFiyat());
                }
            }

            // Kalan detayları kontrol et
            List<SiparisDetay> kalanDetaylar = siparisDetayRepository.findBySiparisFisiId(id);
            if (kalanDetaylar.isEmpty()) {
                siparis.setDurum(SiparisDurum.IPTAL);
            }

            // Toplam tutarı yeniden hesapla
            java.math.BigDecimal hesaplananToplam = kalanDetaylar.stream()
                    .map(SiparisDetay::getToplamFiyat)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            siparis.setToplamTutar(hesaplananToplam);
            siparisFisiRepository.save(siparis);

            return ResponseEntity.ok(Map.of("message", "Sipariş içeriği güncellendi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Güncelleme hatası: " + e.getMessage()));
        }
    }

    // =============== HELPER METHODS ===============
    private Kullanici getAdminFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        String tokenValue = token.substring(7);
        if (tokenValue.startsWith("simple-token-")) {
            try {
                Long userId = Long.parseLong(tokenValue.replace("simple-token-", ""));
                Kullanici kullanici = kullaniciRepository.findById(userId).orElse(null);
                if (kullanici != null && kullanici.getRol() == KullaniciRol.ADMIN) {
                    return kullanici;
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Map<String, Object> createKullaniciResponse(Kullanici k) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", k.getId());
        map.put("kullaniciAdi", k.getKullaniciAdi());
        map.put("email", k.getEmail());
        map.put("ad", k.getAd());
        map.put("soyad", k.getSoyad());
        map.put("telefon", k.getTelefon());
        map.put("rol", k.getRol().toString());
        map.put("aktif", k.getAktif());
        map.put("olusturmaTarihi", k.getOlusturmaTarihi());
        return map;
    }

    private Map<String, Object> createMagazaResponse(Magaza m) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", m.getId());
        map.put("ad", m.getAd());
        map.put("aciklama", m.getAciklama());
        map.put("logoUrl", m.getLogoUrl());
        map.put("aktif", m.getAktif());
        map.put("sahipId", m.getSahip().getId());
        map.put("sahipAd", m.getSahip().getAd() + " " + m.getSahip().getSoyad());
        map.put("olusturmaTarihi", m.getOlusturmaTarihi());
        return map;
    }

    // =============== ÜRÜN GÜNCELLE ===============
    @PostMapping("/urun/{id}/guncelle")
    public ResponseEntity<?> updateUrun(@RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        try {
            Kullanici admin = getAdminFromToken(token);
            if (admin == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Admin yetkisi gerekli"));
            }

            Urun urun = urunRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            if (updates.containsKey("ad")) {
                urun.setAd((String) updates.get("ad"));
            }
            if (updates.containsKey("fiyat")) {
                Object fiyatObj = updates.get("fiyat");
                if (fiyatObj instanceof Number) {
                    urun.setFiyat(java.math.BigDecimal.valueOf(((Number) fiyatObj).doubleValue()));
                } else if (fiyatObj instanceof String) {
                    urun.setFiyat(new java.math.BigDecimal((String) fiyatObj));
                }
            }
            if (updates.containsKey("aciklama")) {
                urun.setAciklama((String) updates.get("aciklama"));
            }

            // Eğer stok güncellemesi de gelirse (opsiyonel, basitlik için şimdilik sadece
            // ürün bilgileri)

            urunRepository.save(urun);

            return ResponseEntity.ok(Map.of("message", "Ürün başarıyla güncellendi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Güncelleme hatası: " + e.getMessage()));
        }
    }

    private Map<String, Object> createSiparisResponse(SiparisFisi s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getId());
        map.put("toplamTutar", s.getToplamTutar());
        map.put("durum", s.getDurum().toString());
        map.put("teslimatAdresi", s.getTeslimatAdresi());
        map.put("siparisTarihi", s.getSiparisTarihi());
        map.put("kullaniciId", s.getKullanici().getId());
        map.put("kullaniciAd", s.getKullanici().getAd() + " " + s.getKullanici().getSoyad());
        map.put("magazaId", s.getMagaza().getId());
        map.put("magazaAd", s.getMagaza().getAd());
        return map;
    }

    private Map<String, Object> createSiparisOzet(SiparisFisi s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getId());
        map.put("toplamTutar", s.getToplamTutar());
        map.put("durum", s.getDurum().toString());
        map.put("siparisTarihi", s.getSiparisTarihi());
        map.put("magazaAd", s.getMagaza().getAd());
        return map;
    }

    // =============== REQUEST CLASSES ===============
    static class UrunEkleRequest {
        private String ad;
        private String aciklama;
        private java.math.BigDecimal fiyat;
        private String renk;
        private String resimUrl;
        private Long altKategoriId;
        private List<StokRequest> stoklar;

        public String getAd() {
            return ad;
        }

        public void setAd(String ad) {
            this.ad = ad;
        }

        public String getAciklama() {
            return aciklama;
        }

        public void setAciklama(String aciklama) {
            this.aciklama = aciklama;
        }

        public java.math.BigDecimal getFiyat() {
            return fiyat;
        }

        public void setFiyat(java.math.BigDecimal fiyat) {
            this.fiyat = fiyat;
        }

        public String getRenk() {
            return renk;
        }

        public void setRenk(String renk) {
            this.renk = renk;
        }

        public String getResimUrl() {
            return resimUrl;
        }

        public void setResimUrl(String resimUrl) {
            this.resimUrl = resimUrl;
        }

        public Long getAltKategoriId() {
            return altKategoriId;
        }

        public void setAltKategoriId(Long altKategoriId) {
            this.altKategoriId = altKategoriId;
        }

        public List<StokRequest> getStoklar() {
            return stoklar;
        }

        public void setStoklar(List<StokRequest> stoklar) {
            this.stoklar = stoklar;
        }
    }

    static class StokRequest {
        private Long bedenId;
        private Integer adet;

        public Long getBedenId() {
            return bedenId;
        }

        public void setBedenId(Long bedenId) {
            this.bedenId = bedenId;
        }

        public Integer getAdet() {
            return adet;
        }

        public void setAdet(Integer adet) {
            this.adet = adet;
        }
    }

    static class KullaniciEkleRequest {
        private String kullaniciAdi;
        private String email;
        private String sifre;
        private String ad;
        private String soyad;
        private String telefon;
        private String adres;
        private String rol; // MUSTERI, MAGAZA_SAHIBI

        public String getKullaniciAdi() {
            return kullaniciAdi;
        }

        public void setKullaniciAdi(String kullaniciAdi) {
            this.kullaniciAdi = kullaniciAdi;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getSifre() {
            return sifre;
        }

        public void setSifre(String sifre) {
            this.sifre = sifre;
        }

        public String getAd() {
            return ad;
        }

        public void setAd(String ad) {
            this.ad = ad;
        }

        public String getSoyad() {
            return soyad;
        }

        public void setSoyad(String soyad) {
            this.soyad = soyad;
        }

        public String getTelefon() {
            return telefon;
        }

        public void setTelefon(String telefon) {
            this.telefon = telefon;
        }

        public String getAdres() {
            return adres;
        }

        public void setAdres(String adres) {
            this.adres = adres;
        }

        public String getRol() {
            return rol;
        }

        public void setRol(String rol) {
            this.rol = rol;
        }
    }

    static class MagazaEkleRequest {
        private String ad;
        private String aciklama;
        private String logoUrl;
        private Long sahipId;

        public String getAd() {
            return ad;
        }

        public void setAd(String ad) {
            this.ad = ad;
        }

        public String getAciklama() {
            return aciklama;
        }

        public void setAciklama(String aciklama) {
            this.aciklama = aciklama;
        }

        public String getLogoUrl() {
            return logoUrl;
        }

        public void setLogoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
        }

        public Long getSahipId() {
            return sahipId;
        }

        public void setSahipId(Long sahipId) {
            this.sahipId = sahipId;
        }
    }
}
