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
@RequestMapping("/api/magaza-sahibi")
@CrossOrigin(origins = "*")
public class MagazaSahibiRestController {

    @Autowired
    private KullaniciRepository kullaniciRepository;

    @Autowired
    private MagazaRepository magazaRepository;

    @Autowired
    private UrunRepository urunRepository;

    @Autowired
    private KategoriRepository kategoriRepository;

    @Autowired
    private AltKategoriRepository altKategoriRepository;

    @Autowired
    private BedenRepository bedenRepository;

    @Autowired
    private UrunStokRepository urunStokRepository;

    @Autowired
    private SiparisFisiRepository siparisFisiRepository;

    @Autowired
    private SiparisDetayRepository siparisDetayRepository;

    // =============== PANEL ÖZETİ ===============
    @GetMapping("/panel")
    public ResponseEntity<?> panel(@RequestHeader("Authorization") String token) {
        try {
            Kullanici sahip = getSahibiFromToken(token);
            if (sahip == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Mağaza sahibi yetkisi gerekli"));
            }

            List<Magaza> magazalar = magazaRepository.findAll().stream()
                    .filter(m -> m.getSahip().getId().equals(sahip.getId()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> magazaOzetleri = magazalar.stream()
                    .map(magaza -> {
                        Map<String, Object> ozet = new HashMap<>();
                        ozet.put("id", magaza.getId());
                        ozet.put("ad", magaza.getAd());
                        ozet.put("logoUrl", magaza.getLogoUrl());
                        ozet.put("aktif", magaza.getAktif());

                        long urunSayisi = urunRepository.findByMagazaId(magaza.getId()).size();
                        ozet.put("urunSayisi", urunSayisi);

                        List<SiparisFisi> siparisler = siparisFisiRepository.findByMagazaId(magaza.getId());
                        ozet.put("siparisSayisi", siparisler.size());

                        long bekleyenSiparis = siparisler.stream()
                                .filter(s -> s.getDurum() == SiparisDurum.BEKLEMEDE).count();
                        ozet.put("bekleyenSiparis", bekleyenSiparis);

                        BigDecimal ciro = siparisler.stream()
                                .filter(s -> s.getDurum() != SiparisDurum.IPTAL)
                                .map(s -> s.getToplamTutar() != null ? s.getToplamTutar() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        ozet.put("toplamCiro", ciro);

                        return ozet;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "sahipAd", sahip.getAd() + " " + sahip.getSoyad(),
                    "magazalar", magazaOzetleri));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Panel getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA OLUŞTUR ===============
    @PostMapping("/magaza")
    @Transactional
    public ResponseEntity<?> magazaOlustur(@RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {
        try {
            Kullanici sahip = getSahibiFromToken(token);
            if (sahip == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Mağaza sahibi yetkisi gerekli"));
            }

            String ad = request.get("ad");
            String aciklama = request.get("aciklama");

            if (ad == null || ad.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mağaza adı zorunludur"));
            }

            Magaza magaza = new Magaza();
            magaza.setAd(ad.trim());
            magaza.setAciklama(aciklama != null ? aciklama.trim() : "");
            magaza.setSahip(sahip);
            magaza.setAktif(true);

            magazaRepository.save(magaza);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Mağaza başarıyla oluşturuldu",
                    "magazaId", magaza.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mağaza oluşturulurken hata: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA SİL ===============
    @DeleteMapping("/magaza/{magazaId}")
    @Transactional
    public ResponseEntity<?> magazaSil(@RequestHeader("Authorization") String token,
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

            // Önce ürünlerin stoklarını sil
            List<Urun> urunler = urunRepository.findByMagazaId(magazaId);
            for (Urun urun : urunler) {
                urunStokRepository.deleteAll(urunStokRepository.findByUrunId(urun.getId()));
            }

            // Ürünleri sil
            urunRepository.deleteAll(urunler);

            // Mağazayı sil
            magazaRepository.delete(magaza);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Mağaza ve tüm ürünleri silindi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mağaza silinirken hata: " + e.getMessage()));
        }
    }

    // =============== MAĞAZANIN ÜRÜNLERİ ===============
    @GetMapping("/magaza/{magazaId}/urunler")
    public ResponseEntity<?> magazaUrunleri(@RequestHeader("Authorization") String token,
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

            List<Urun> urunler = urunRepository.findByMagazaId(magazaId);

            List<Map<String, Object>> response = urunler.stream()
                    .map(this::createUrunResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ürünler getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== ÜRÜN EKLE ===============
    @PostMapping("/magaza/{magazaId}/urun")
    @Transactional
    public ResponseEntity<?> urunEkle(@RequestHeader("Authorization") String token,
            @PathVariable Long magazaId,
            @RequestBody UrunEkleRequest request) {
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

            // Alt kategori - zorunlu değil, yoksa ilk alt kategoriyi kullan
            AltKategori altKategori;
            if (request.getAltKategoriId() != null) {
                altKategori = altKategoriRepository.findById(request.getAltKategoriId())
                        .orElseThrow(() -> new RuntimeException("Alt kategori bulunamadı"));
            } else {
                // Varsayılan alt kategori al
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

            return ResponseEntity.ok(Map.of(
                    "message", "Ürün eklendi",
                    "urun", createUrunResponse(urun)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ürün eklenirken hata: " + e.getMessage()));
        }
    }

    // =============== ÜRÜN GÜNCELLE ===============
    @PutMapping("/urun/{urunId}")
    @Transactional
    public ResponseEntity<?> urunGuncelle(@RequestHeader("Authorization") String token,
            @PathVariable Long urunId,
            @RequestBody UrunEkleRequest request) {
        try {
            Kullanici sahip = getSahibiFromToken(token);
            if (sahip == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Mağaza sahibi yetkisi gerekli"));
            }

            Urun urun = urunRepository.findById(urunId)
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            if (!urun.getMagaza().getSahip().getId().equals(sahip.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu ürün size ait bir mağazada değil"));
            }

            if (request.getAd() != null)
                urun.setAd(request.getAd());
            if (request.getAciklama() != null)
                urun.setAciklama(request.getAciklama());
            if (request.getFiyat() != null)
                urun.setFiyat(request.getFiyat());
            if (request.getRenk() != null)
                urun.setRenk(request.getRenk());
            if (request.getResimUrl() != null)
                urun.setResimUrl(request.getResimUrl());

            if (request.getAltKategoriId() != null) {
                AltKategori altKategori = altKategoriRepository.findById(request.getAltKategoriId())
                        .orElseThrow(() -> new RuntimeException("Alt kategori bulunamadı"));
                urun.setAltKategori(altKategori);
            }

            urunRepository.save(urun);

            // Stokları güncelle
            if (request.getStoklar() != null) {
                urunStokRepository.deleteByUrunId(urunId);
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

            return ResponseEntity.ok(Map.of(
                    "message", "Ürün güncellendi",
                    "urun", createUrunResponse(urun)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ürün güncellenirken hata: " + e.getMessage()));
        }
    }

    // =============== ÜRÜN AKTİF/PASİF ===============
    @PutMapping("/urun/{urunId}/durum")
    public ResponseEntity<?> urunDurumDegistir(@RequestHeader("Authorization") String token,
            @PathVariable Long urunId) {
        try {
            Kullanici sahip = getSahibiFromToken(token);
            if (sahip == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Mağaza sahibi yetkisi gerekli"));
            }

            Urun urun = urunRepository.findById(urunId)
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            if (!urun.getMagaza().getSahip().getId().equals(sahip.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu ürün size ait değil"));
            }

            urun.setAktif(!urun.getAktif());
            urunRepository.save(urun);

            return ResponseEntity.ok(Map.of(
                    "message", urun.getAktif() ? "Ürün aktifleştirildi" : "Ürün pasifleştirildi",
                    "urun", createUrunResponse(urun)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Durum değiştirilirken hata: " + e.getMessage()));
        }
    }

    // =============== ÜRÜN SİL ===============
    @DeleteMapping("/urun/{urunId}")
    @Transactional
    public ResponseEntity<?> urunSil(@RequestHeader("Authorization") String token,
            @PathVariable Long urunId) {
        try {
            Kullanici sahip = getSahibiFromToken(token);
            if (sahip == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Mağaza sahibi yetkisi gerekli"));
            }

            Urun urun = urunRepository.findById(urunId)
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            if (!urun.getMagaza().getSahip().getId().equals(sahip.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu ürün size ait değil"));
            }

            urunStokRepository.deleteByUrunId(urunId);
            urunRepository.delete(urun);

            return ResponseEntity.ok(Map.of("message", "Ürün silindi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ürün silinirken hata: " + e.getMessage()));
        }
    }

    // =============== MAĞAZA SİPARİŞLERİ ===============
    @GetMapping("/magaza/{magazaId}/siparisler")
    public ResponseEntity<?> magazaSiparisleri(@RequestHeader("Authorization") String token,
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

            List<SiparisFisi> siparisler = siparisFisiRepository.findByMagazaId(magazaId).stream()
                    .sorted((a, b) -> b.getSiparisTarihi().compareTo(a.getSiparisTarihi()))
                    .collect(Collectors.toList());

            List<Map<String, Object>> response = siparisler.stream()
                    .map(this::createSiparisResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Siparişler getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== SİPARİŞ DURUMU GÜNCELLE ===============
    @PutMapping("/siparis/{siparisId}/durum")
    public ResponseEntity<?> siparisDurumuGuncelle(@RequestHeader("Authorization") String token,
            @PathVariable Long siparisId,
            @RequestBody Map<String, String> request) {
        try {
            Kullanici sahip = getSahibiFromToken(token);
            if (sahip == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Mağaza sahibi yetkisi gerekli"));
            }

            SiparisFisi siparis = siparisFisiRepository.findById(siparisId)
                    .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

            if (!siparis.getMagaza().getSahip().getId().equals(sahip.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu sipariş size ait bir mağazada değil"));
            }

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

    // =============== KATEGORİLER VE BEDENLER (Form için) ===============
    @GetMapping("/form-data")
    public ResponseEntity<?> formData(@RequestHeader("Authorization") String token) {
        try {
            Kullanici sahip = getSahibiFromToken(token);
            if (sahip == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Mağaza sahibi yetkisi gerekli"));
            }

            List<Map<String, Object>> kategoriler = kategoriRepository.findAll().stream()
                    .map(k -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", k.getId());
                        map.put("ad", k.getAd());
                        return map;
                    }).collect(Collectors.toList());

            List<Map<String, Object>> altKategoriler = altKategoriRepository.findAll().stream()
                    .map(ak -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", ak.getId());
                        map.put("ad", ak.getAd());
                        map.put("kategoriId", ak.getKategori().getId());
                        map.put("sezon", ak.getSezon().toString());
                        return map;
                    }).collect(Collectors.toList());

            List<Map<String, Object>> bedenler = bedenRepository.findAll().stream()
                    .map(b -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", b.getId());
                        map.put("ad", b.getAd());
                        return map;
                    }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "kategoriler", kategoriler,
                    "altKategoriler", altKategoriler,
                    "bedenler", bedenler));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Form verileri getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== HELPER METHODS ===============
    private Kullanici getSahibiFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        String tokenValue = token.substring(7);
        if (tokenValue.startsWith("simple-token-")) {
            try {
                Long userId = Long.parseLong(tokenValue.replace("simple-token-", ""));
                Kullanici kullanici = kullaniciRepository.findById(userId).orElse(null);
                if (kullanici != null &&
                        (kullanici.getRol() == KullaniciRol.MAGAZA_SAHIBI
                                || kullanici.getRol() == KullaniciRol.ADMIN)) {
                    return kullanici;
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Map<String, Object> createUrunResponse(Urun urun) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", urun.getId());
        map.put("ad", urun.getAd());
        map.put("aciklama", urun.getAciklama());
        map.put("fiyat", urun.getFiyat());
        map.put("renk", urun.getRenk());
        map.put("resimUrl", urun.getResimUrl());
        map.put("aktif", urun.getAktif());
        map.put("altKategoriId", urun.getAltKategori().getId());
        map.put("altKategoriAd", urun.getAltKategori().getAd());
        map.put("kategoriAd", urun.getAltKategori().getKategori().getAd());

        List<UrunStok> stoklar = urunStokRepository.findByUrunId(urun.getId());
        List<Map<String, Object>> stokListesi = stoklar.stream()
                .map(s -> {
                    Map<String, Object> stokMap = new HashMap<>();
                    stokMap.put("bedenId", s.getBeden().getId());
                    stokMap.put("bedenAd", s.getBeden().getAd());
                    stokMap.put("adet", s.getAdet());
                    return stokMap;
                }).collect(Collectors.toList());
        map.put("stoklar", stokListesi);

        return map;
    }

    private Map<String, Object> createSiparisResponse(SiparisFisi s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getId());
        map.put("toplamTutar", s.getToplamTutar());
        map.put("durum", s.getDurum().toString());
        map.put("teslimatAdresi", s.getTeslimatAdresi());
        map.put("siparisTarihi", s.getSiparisTarihi());
        map.put("kullaniciAd", s.getKullanici().getAd() + " " + s.getKullanici().getSoyad());
        map.put("kullaniciTelefon", s.getKullanici().getTelefon());

        List<SiparisDetay> detaylar = siparisDetayRepository.findBySiparisFisiId(s.getId());
        List<Map<String, Object>> detayListesi = detaylar.stream()
                .map(d -> {
                    Map<String, Object> detayMap = new HashMap<>();
                    detayMap.put("id", d.getId());
                    detayMap.put("urunAd", d.getUrun().getAd());
                    detayMap.put("bedenAd", d.getBeden().getAd());
                    detayMap.put("adet", d.getAdet());
                    detayMap.put("birimFiyat", d.getBirimFiyat());
                    detayMap.put("toplamFiyat", d.getToplamFiyat());
                    return detayMap;
                }).collect(Collectors.toList());
        map.put("detaylar", detayListesi);

        return map;
    }

    // =============== REQUEST CLASSES ===============
    static class UrunEkleRequest {
        private String ad;
        private String aciklama;
        private BigDecimal fiyat;
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

        public BigDecimal getFiyat() {
            return fiyat;
        }

        public void setFiyat(BigDecimal fiyat) {
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
        private int adet;

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

    static class SiparisIcerikGuncelleRequest {
        private List<Long> detayIds;
        private List<Integer> adetler;
        private List<Long> silinecekIds;

        public List<Long> getDetayIds() {
            return detayIds;
        }

        public void setDetayIds(List<Long> detayIds) {
            this.detayIds = detayIds;
        }

        public List<Integer> getAdetler() {
            return adetler;
        }

        public void setAdetler(List<Integer> adetler) {
            this.adetler = adetler;
        }

        public List<Long> getSilinecekIds() {
            return silinecekIds;
        }

        public void setSilinecekIds(List<Long> silinecekIds) {
            this.silinecekIds = silinecekIds;
        }
    }

    // =============== SİPARİŞ DETAY GETİR ===============
    @GetMapping("/siparis/{siparisId}")
    public ResponseEntity<?> getSiparis(@RequestHeader("Authorization") String token,
            @PathVariable Long siparisId) {
        try {
            Kullanici sahip = getSahibiFromToken(token);
            if (sahip == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Mağaza sahibi yetkisi gerekli"));
            }

            SiparisFisi siparis = siparisFisiRepository.findById(siparisId)
                    .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

            if (!siparis.getMagaza().getSahip().getId().equals(sahip.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu sipariş size ait bir mağazada değil"));
            }

            return ResponseEntity.ok(createSiparisResponse(siparis));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Sipariş getirilirken hata: " + e.getMessage()));
        }
    }

    // =============== SİPARİŞ İÇERİK GÜNCELLE ===============
    @PutMapping("/siparis/{siparisId}/icerik")
    @Transactional
    public ResponseEntity<?> siparisIcerikGuncelle(@RequestHeader("Authorization") String token,
            @PathVariable Long siparisId,
            @RequestBody SiparisIcerikGuncelleRequest request) {
        try {
            Kullanici sahip = getSahibiFromToken(token);
            if (sahip == null) {
                return ResponseEntity.status(403).body(Map.of("error", "Mağaza sahibi yetkisi gerekli"));
            }

            SiparisFisi siparis = siparisFisiRepository.findById(siparisId)
                    .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

            if (!siparis.getMagaza().getSahip().getId().equals(sahip.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bu sipariş size ait bir mağazada değil"));
            }

            List<Long> detayIds = request.getDetayIds();
            List<Integer> adetler = request.getAdetler();
            List<Long> silinecekIds = request.getSilinecekIds();

            if (detayIds == null || adetler == null || detayIds.size() != adetler.size()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Detay ID'leri ve adetler eşleşmiyor"));
            }

            for (int i = 0; i < detayIds.size(); i++) {
                Long detayId = detayIds.get(i);
                Integer yeniAdet = adetler.get(i);

                if (yeniAdet < 1) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Adet 1'den küçük olamaz!"));
                }

                SiparisDetay detay = siparisDetayRepository.findById(detayId)
                        .orElseThrow(() -> new RuntimeException("Detay bulunamadı"));

                // Detay siparişe ait mi kontrol et
                if (!detay.getSiparisFisi().getId().equals(siparisId)) {
                    continue;
                }

                // Silinecek mi?
                if (silinecekIds != null && silinecekIds.contains(detayId)) {
                    // Stok iade
                    UrunStok stok = urunStokRepository
                            .findByUrunIdAndBedenId(detay.getUrun().getId(), detay.getBeden().getId())
                            .orElse(new UrunStok(detay.getUrun(), detay.getBeden(), 0));
                    stok.setAdet(stok.getAdet() + detay.getAdet());
                    urunStokRepository.save(stok);

                    siparisDetayRepository.delete(detay);
                } else {
                    // Adet değişti mi?
                    if (!detay.getAdet().equals(yeniAdet)) {
                        int fark = detay.getAdet() - yeniAdet; // pozitif = iade, negatif = düş

                        UrunStok stok = urunStokRepository
                                .findByUrunIdAndBedenId(detay.getUrun().getId(), detay.getBeden().getId())
                                .orElse(new UrunStok(detay.getUrun(), detay.getBeden(), 0));

                        if (stok.getAdet() + fark < 0) {
                            return ResponseEntity.badRequest()
                                    .body(Map.of("error", "Yetersiz stok! Ürün: " + detay.getUrun().getAd()));
                        }

                        stok.setAdet(stok.getAdet() + fark);
                        urunStokRepository.save(stok);

                        detay.setAdet(yeniAdet);
                        detay.setToplamFiyat(detay.getBirimFiyat().multiply(BigDecimal.valueOf(yeniAdet)));
                        siparisDetayRepository.save(detay);
                    }
                }
            }

            // Toplam tutarı güncelle
            // EntityManager flush gerekebilir, save sonrası tekrar çekelim
            siparisDetayRepository.flush();

            List<SiparisDetay> guncelDetaylar = siparisDetayRepository.findBySiparisFisiId(siparisId);
            if (guncelDetaylar.isEmpty()) {
                siparis.setToplamTutar(BigDecimal.ZERO);
                siparis.setDurum(SiparisDurum.IPTAL);
            } else {
                BigDecimal yeniToplam = guncelDetaylar.stream()
                        .map(SiparisDetay::getToplamFiyat)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                siparis.setToplamTutar(yeniToplam);
            }
            siparisFisiRepository.save(siparis);

            return ResponseEntity.ok(Map.of(
                    "message", "Sipariş içeriği güncellendi",
                    "siparis", createSiparisResponse(siparis)));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Güncelleme hatası: " + e.getMessage()));
        }
    }
}
