package com.magazaapp.controller;

import com.magazaapp.model.*;
import com.magazaapp.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final KullaniciRepository kullaniciRepository;
    private final MagazaRepository magazaRepository;
    private final UrunRepository urunRepository;
    private final SiparisFisiRepository siparisFisiRepository;
    private final MesajRepository mesajRepository;
    private final KategoriRepository kategoriRepository;
    private final AltKategoriRepository altKategoriRepository;
    private final BedenRepository bedenRepository;
    private final UrunStokRepository urunStokRepository;
    private final SiparisDetayRepository siparisDetayRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(KullaniciRepository kullaniciRepository, MagazaRepository magazaRepository,
            UrunRepository urunRepository, SiparisFisiRepository siparisFisiRepository,
            MesajRepository mesajRepository, KategoriRepository kategoriRepository,
            AltKategoriRepository altKategoriRepository, BedenRepository bedenRepository,
            UrunStokRepository urunStokRepository, SiparisDetayRepository siparisDetayRepository,
            PasswordEncoder passwordEncoder) {
        this.kullaniciRepository = kullaniciRepository;
        this.magazaRepository = magazaRepository;
        this.urunRepository = urunRepository;
        this.siparisFisiRepository = siparisFisiRepository;
        this.mesajRepository = mesajRepository;
        this.kategoriRepository = kategoriRepository;
        this.altKategoriRepository = altKategoriRepository;
        this.bedenRepository = bedenRepository;
        this.urunStokRepository = urunStokRepository;
        this.siparisDetayRepository = siparisDetayRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ============ DASHBOARD ============
    @GetMapping
    public String dashboard(Model model) {
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

        List<SiparisFisi> sonSiparisler = siparisFisiRepository.findAll().stream()
                .sorted((a, b) -> b.getSiparisTarihi().compareTo(a.getSiparisTarihi()))
                .limit(5).toList();

        List<Kullanici> sonKullanicilar = kullaniciRepository.findAll().stream()
                .sorted((a, b) -> b.getOlusturmaTarihi().compareTo(a.getOlusturmaTarihi()))
                .limit(5).toList();

        model.addAttribute("toplamKullanici", toplamKullanici);
        model.addAttribute("toplamMagaza", toplamMagaza);
        model.addAttribute("toplamUrun", toplamUrun);
        model.addAttribute("toplamSiparis", toplamSiparis);
        model.addAttribute("bekleyenSiparis", bekleyenSiparis);
        model.addAttribute("toplamCiro", toplamCiro);
        model.addAttribute("sonSiparisler", sonSiparisler);
        model.addAttribute("sonKullanicilar", sonKullanicilar);

        return "admin/panel";
    }

    // ============ KULLANICI YÖNETİMİ ============
    @GetMapping("/kullanicilar")
    public String kullaniciListesi(Model model) {
        List<Kullanici> kullanicilar = kullaniciRepository.findAll();
        model.addAttribute("kullanicilar", kullanicilar);
        return "admin/kullanicilar";
    }

    @PostMapping("/kullanici/{id}/rol-degistir")
    public String rolDegistir(@PathVariable Long id, @RequestParam String yeniRol,
            RedirectAttributes redirectAttributes) {
        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        try {
            KullaniciRol rol = KullaniciRol.valueOf(yeniRol);
            kullanici.setRol(rol);
            kullaniciRepository.save(kullanici);
            redirectAttributes.addFlashAttribute("basari",
                    kullanici.getKullaniciAdi() + " kullanıcısının rolü '" + rol + "' olarak güncellendi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Rol değiştirme hatası: " + e.getMessage());
        }

        return "redirect:/admin/kullanicilar";
    }

    @PostMapping("/kullanici/{id}/sil")
    public String kullaniciSil(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Kullanici kullanici = kullaniciRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            if (kullanici.getRol() == KullaniciRol.ADMIN) {
                redirectAttributes.addFlashAttribute("hata", "Admin kullanıcıları silinemez!");
                return "redirect:/admin/kullanicilar";
            }

            // Mağaza sahibi kontrolü
            if (kullanici.getRol() == KullaniciRol.MAGAZA_SAHIBI) {
                List<Magaza> magazalar = magazaRepository.findAll().stream()
                        .filter(m -> m.getSahip() != null && m.getSahip().getId().equals(id))
                        .toList();
                if (!magazalar.isEmpty()) {
                    redirectAttributes.addFlashAttribute("hata",
                            "Bu kullanıcının " + magazalar.size()
                                    + " mağazası var! Önce mağazaları silmeniz gerekiyor.");
                    return "redirect:/admin/kullanicilar";
                }
            }

            String kullaniciAdi = kullanici.getKullaniciAdi();
            kullaniciRepository.delete(kullanici);
            redirectAttributes.addFlashAttribute("basari", kullaniciAdi + " kullanıcısı silindi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Silme hatası: " + e.getMessage());
        }

        return "redirect:/admin/kullanicilar";
    }

    @GetMapping("/kullanici/{id}/duzenle")
    public String kullaniciDuzenleForm(@PathVariable Long id, Model model,
            org.springframework.security.core.Authentication auth) {
        // Düzenlenecek kullanıcı
        Kullanici duzenlenecek = kullaniciRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        model.addAttribute("duzenlenecekKullanici", duzenlenecek);

        // Navbar için giriş yapan admin
        Kullanici admin = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Admin bulunamadı"));
        model.addAttribute("kullanici", admin);

        return "admin/kullanici-duzenle";
    }

    @PostMapping("/kullanici/{id}/duzenle")
    public String kullaniciDuzenle(@PathVariable Long id, @RequestParam String ad, @RequestParam String soyad,
            @RequestParam String email, @RequestParam String telefon,
            RedirectAttributes redirectAttributes) {
        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        kullanici.setAd(ad);
        kullanici.setSoyad(soyad);
        kullanici.setEmail(email);
        kullanici.setTelefon(telefon);
        kullaniciRepository.save(kullanici);

        redirectAttributes.addFlashAttribute("basari", "Kullanıcı bilgileri güncellendi.");
        return "redirect:/admin/kullanicilar";
    }

    @PostMapping("/kullanici/ekle")
    public String kullaniciEkle(@RequestParam String kullaniciAdi,
            @RequestParam String email,
            @RequestParam String sifre,
            @RequestParam String ad,
            @RequestParam String soyad,
            @RequestParam(required = false) String telefon,
            @RequestParam(required = false) String adres,
            @RequestParam String rol,
            RedirectAttributes redirectAttributes) {

        try {
            if (kullaniciRepository.findByKullaniciAdi(kullaniciAdi).isPresent()) {
                throw new RuntimeException("Bu kullanıcı adı zaten kullanılıyor!");
            }
            if (kullaniciRepository.findByEmail(email).isPresent()) {
                throw new RuntimeException("Bu e-posta adresi zaten kullanılıyor!");
            }

            Kullanici kullanici = new Kullanici();
            kullanici.setKullaniciAdi(kullaniciAdi);
            kullanici.setEmail(email);
            kullanici.setSifre(passwordEncoder.encode(sifre));
            kullanici.setAd(ad);
            kullanici.setSoyad(soyad);
            kullanici.setTelefon(telefon);
            kullanici.setAdres(adres);
            kullanici.setRol(KullaniciRol.valueOf(rol));

            kullaniciRepository.save(kullanici);
            redirectAttributes.addFlashAttribute("basari", "Kullanıcı başarıyla oluşturuldu.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Kullanıcı oluşturma hatası: " + e.getMessage());
        }

        return "redirect:/admin/kullanicilar";
    }

    // ============ MAĞAZA YÖNETİMİ ============
    @GetMapping("/magazalar")
    public String magazaListesi(Model model) {
        List<Magaza> magazalar = magazaRepository.findAll();
        model.addAttribute("magazalar", magazalar);
        return "admin/magazalar";
    }

    @PostMapping("/magaza/{id}/durum-degistir")
    public String magazaDurumDegistir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Magaza magaza = magazaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            magaza.setAktif(!magaza.getAktif());
            magazaRepository.save(magaza);

            String durum = magaza.getAktif() ? "aktif" : "pasif";
            redirectAttributes.addFlashAttribute("basari",
                    magaza.getAd() + " mağazası " + durum + " yapıldı.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Durum değiştirme hatası: " + e.getMessage());
        }

        return "redirect:/admin/magazalar";
    }

    @PostMapping("/magaza/{id}/sil")
    public String magazaSil(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Magaza magaza = magazaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

            String magazaAd = magaza.getAd();
            magazaRepository.delete(magaza);
            redirectAttributes.addFlashAttribute("basari", magazaAd + " mağazası silindi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata",
                    "Mağaza silinemedi. Aktif ürünler veya siparişler olabilir: " + e.getMessage());
        }

        return "redirect:/admin/magazalar";
    }

    // ============ SİPARİŞ YÖNETİMİ ============
    @GetMapping("/siparisler")
    public String siparisListesi(@RequestParam(required = false) String durum,
            @RequestParam(required = false) Long magazaId,
            Model model) {
        List<SiparisFisi> siparisler;

        if (durum != null && !durum.isEmpty()) {
            try {
                SiparisDurum siparisDurum = SiparisDurum.valueOf(durum);
                siparisler = siparisFisiRepository.findAll().stream()
                        .filter(s -> s.getDurum() == siparisDurum).toList();
            } catch (Exception e) {
                siparisler = siparisFisiRepository.findAll();
            }
        } else {
            siparisler = siparisFisiRepository.findAll();
        }

        // Mağaza filtresi
        if (magazaId != null) {
            siparisler = siparisler.stream()
                    .filter(s -> s.getMagaza() != null && s.getMagaza().getId().equals(magazaId))
                    .toList();
            magazaRepository.findById(magazaId).ifPresent(m -> model.addAttribute("filtreMagaza", m));
        }

        siparisler = siparisler.stream()
                .sorted((a, b) -> b.getSiparisTarihi().compareTo(a.getSiparisTarihi()))
                .toList();

        model.addAttribute("siparisler", siparisler);
        model.addAttribute("seciliDurum", durum);
        model.addAttribute("durumlar", SiparisDurum.values());

        return "admin/siparisler";
    }

    @PostMapping("/siparis/{id}/durum-guncelle")
    public String siparisDurumGuncelle(@PathVariable Long id, @RequestParam String yeniDurum,
            RedirectAttributes redirectAttributes) {
        try {
            SiparisFisi siparis = siparisFisiRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

            SiparisDurum durum = SiparisDurum.valueOf(yeniDurum);
            siparis.setDurum(durum);
            siparisFisiRepository.save(siparis);

            redirectAttributes.addFlashAttribute("basari",
                    "Sipariş #" + id + " durumu '" + durum + "' olarak güncellendi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Durum güncelleme hatası: " + e.getMessage());
        }

        return "redirect:/admin/siparisler";
    }

    // ============ SİPARİŞ DÜZENLEME (ADMİN) ============
    @GetMapping("/siparis/{id}/duzenle")
    public String siparisDuzenleForm(@PathVariable Long id, Model model,
            org.springframework.security.core.Authentication auth) {
        SiparisFisi siparis = siparisFisiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

        List<SiparisDetay> siparisDetaylari = siparisDetayRepository.findBySiparisFisiId(siparis.getId());

        model.addAttribute("siparis", siparis);
        model.addAttribute("siparisDetaylari", siparisDetaylari);
        model.addAttribute("durumlar", SiparisDurum.values());

        // Navbar için giriş yapan admin
        Kullanici admin = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Admin bulunamadı"));
        model.addAttribute("kullanici", admin);

        return "admin/siparis-duzenle";
    }

    @PostMapping("/siparis/{id}/duzenle")
    public String siparisDuzenle(@PathVariable Long id,
            @RequestParam SiparisDurum durum,
            @RequestParam String teslimatAdresi,
            RedirectAttributes redirectAttributes) {
        SiparisFisi siparis = siparisFisiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

        siparis.setDurum(durum);
        siparis.setTeslimatAdresi(teslimatAdresi);
        siparisFisiRepository.save(siparis);

        redirectAttributes.addFlashAttribute("basari", "Sipariş #" + id + " bilgileri güncellendi.");
        return "redirect:/admin/siparisler";
    }

    @PostMapping("/siparis/{id}/icerik-guncelle")
    public String siparisIcerikGuncelle(@PathVariable Long id,
            @RequestParam List<Long> detayIds,
            @RequestParam List<Integer> adetler,
            @RequestParam(required = false) List<Long> silinecekIds,
            RedirectAttributes redirectAttributes) {
        try {
            SiparisFisi siparis = siparisFisiRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

            for (int i = 0; i < detayIds.size(); i++) {
                Long detayId = detayIds.get(i);
                Integer yeniAdet = adetler.get(i);

                if (yeniAdet < 1) {
                    throw new RuntimeException("Adet 1'den küçük olamaz!");
                }

                SiparisDetay detay = siparisDetayRepository.findById(detayId)
                        .orElseThrow(() -> new RuntimeException("Detay bulunamadı"));

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
                            throw new RuntimeException("Yetersiz stok! Ürün: " + detay.getUrun().getAd() + " Beden: "
                                    + detay.getBeden().getAd());
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
            List<SiparisDetay> guncelDetaylar = siparisDetayRepository.findBySiparisFisiId(id);
            if (guncelDetaylar.isEmpty()) {
                siparis.setToplamTutar(BigDecimal.ZERO);
                // Detayı kalmayan siparişi iptal et veya o şekilde bırak? İptal etmek mantıklı.
                siparis.setDurum(SiparisDurum.IPTAL);
                redirectAttributes.addFlashAttribute("uyari", "Tüm ürünler silindiği için sipariş iptal edildi.");
            } else {
                BigDecimal yeniToplam = guncelDetaylar.stream()
                        .map(SiparisDetay::getToplamFiyat)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                siparis.setToplamTutar(yeniToplam);
            }
            siparisFisiRepository.save(siparis);

            redirectAttributes.addFlashAttribute("basari", "Sipariş içeriği güncellendi.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Güncelleme hatası: " + e.getMessage());
        }

        return "redirect:/admin/siparis/" + id + "/duzenle";
    }

    // ============ CİRO RAPORU ============
    @GetMapping("/ciro")
    public String ciroRaporu(@RequestParam(required = false, defaultValue = "TUMU") String donem, Model model) {
        // Tarih filtresi için başlangıç zamanını belirle
        java.time.LocalDateTime baslangicTarihi;
        java.time.LocalDateTime simdi = java.time.LocalDateTime.now();

        if ("HAFTALIK".equals(donem)) {
            baslangicTarihi = simdi.minusWeeks(1);
        } else if ("AYLIK".equals(donem)) {
            baslangicTarihi = simdi.minusMonths(1);
        } else if ("YILLIK".equals(donem)) {
            baslangicTarihi = simdi.minusYears(1);
        } else {
            baslangicTarihi = java.time.LocalDateTime.of(2000, 1, 1, 0, 0); // Tüm zamanlar
        }

        // Tüm siparişleri çekip bellekte filtrele (performans için repository query
        // daha iyi olurdu ama şimdilik ok)
        List<SiparisFisi> tumSiparisler = siparisFisiRepository.findAll().stream()
                .filter(s -> s.getSiparisTarihi().isAfter(baslangicTarihi))
                .toList();

        // Genel İstatistikler
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

        // Mağaza Bazlı Rapor Hazırla
        List<Magaza> magazalar = magazaRepository.findAll();
        List<java.util.Map<String, Object>> magazaRaporlari = new java.util.ArrayList<>();

        for (Magaza m : magazalar) {
            java.util.Map<String, Object> rapor = new java.util.HashMap<>();
            rapor.put("magaza", m);

            List<SiparisFisi> magazaSiparisleri = tumSiparisler.stream()
                    .filter(s -> s.getMagaza().getId().equals(m.getId()))
                    .toList();

            rapor.put("siparisSayisi", magazaSiparisleri.size());

            BigDecimal magazaCiro = magazaSiparisleri.stream()
                    .filter(s -> s.getDurum() != SiparisDurum.IPTAL)
                    .map(s -> s.getToplamTutar() != null ? s.getToplamTutar() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            rapor.put("ciro", magazaCiro);
            magazaRaporlari.add(rapor);
        }

        // Ciroya göre sırala (Azalan)
        magazaRaporlari.sort((r1, r2) -> ((BigDecimal) r2.get("ciro")).compareTo((BigDecimal) r1.get("ciro")));

        model.addAttribute("toplamCiro", toplamCiro);
        model.addAttribute("tamamlananCiro", tamamlananCiro);
        model.addAttribute("bekleyenCiro", bekleyenCiro);
        model.addAttribute("magazaRaporlari", magazaRaporlari);
        model.addAttribute("seciliDonem", donem);

        return "admin/ciro";
    }

    // ============ MAĞAZA DÜZENLEME (ADMİN) ============
    @GetMapping("/magaza/{id}/duzenle")
    public String magazaDuzenleForm(@PathVariable Long id, Model model,
            org.springframework.security.core.Authentication auth) {
        Magaza magaza = magazaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));
        model.addAttribute("magaza", magaza);

        // Navbar için giriş yapan admin
        Kullanici admin = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Admin bulunamadı"));
        model.addAttribute("kullanici", admin);

        return "admin/magaza-duzenle";
    }

    @PostMapping("/magaza/{id}/duzenle")
    public String magazaDuzenle(@PathVariable Long id, @RequestParam String ad, @RequestParam String aciklama,
            @RequestParam String logoUrl, RedirectAttributes redirectAttributes) {
        Magaza magaza = magazaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

        magaza.setAd(ad);
        magaza.setAciklama(aciklama);
        magaza.setLogoUrl(logoUrl);
        magazaRepository.save(magaza);

        redirectAttributes.addFlashAttribute("basari", "Mağaza bilgileri güncellendi.");
        return "redirect:/admin/magazalar";
    }

    @GetMapping("/magaza/{id}/urunler")
    public String magazaUrunleri(@PathVariable Long id, Model model) {
        Magaza magaza = magazaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));
        List<Urun> urunler = urunRepository.findByMagazaId(id);
        model.addAttribute("magaza", magaza);
        model.addAttribute("urunler", urunler);
        return "admin/magaza-urunler";
    }

    // ============ ÜRÜN DÜZENLEME (ADMİN) ============
    @GetMapping("/urun/{id}/duzenle")
    public String urunDuzenleForm(@PathVariable Long id, Model model,
            org.springframework.security.core.Authentication auth) {
        Urun urun = urunRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

        List<Kategori> kategoriler = kategoriRepository.findAll();
        List<AltKategori> altKategoriler = altKategoriRepository.findAll();
        List<Beden> bedenler = bedenRepository.findAll();
        List<UrunStok> mevcutStoklar = urunStokRepository.findByUrunId(id);

        model.addAttribute("urun", urun);
        model.addAttribute("magaza", urun.getMagaza());
        model.addAttribute("kategoriler", kategoriler);
        model.addAttribute("altKategoriler", altKategoriler);
        model.addAttribute("bedenler", bedenler);
        model.addAttribute("mevcutStoklar", mevcutStoklar);

        // Navbar için giriş yapan admin
        Kullanici admin = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Admin bulunamadı"));
        model.addAttribute("kullanici", admin);

        return "admin/urun-duzenle";
    }

    @PostMapping("/urun/{id}/duzenle")
    public String urunDuzenle(@PathVariable Long id,
            @RequestParam String ad,
            @RequestParam String aciklama,
            @RequestParam BigDecimal fiyat,
            @RequestParam String renk,
            @RequestParam(required = false) String resimUrl,
            @RequestParam Long altKategoriId,
            @RequestParam(required = false) List<Long> bedenIds,
            @RequestParam(required = false) List<Integer> stoklar,
            RedirectAttributes redirectAttributes) {

        Urun urun = urunRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

        AltKategori altKategori = altKategoriRepository.findById(altKategoriId)
                .orElseThrow(() -> new RuntimeException("Alt kategori bulunamadı"));

        // Ürün Güncelle
        urun.setAd(ad);
        urun.setAciklama(aciklama);
        urun.setFiyat(fiyat);
        urun.setRenk(renk);
        urun.setResimUrl(resimUrl);
        urun.setAltKategori(altKategori);
        urunRepository.save(urun);

        // Stokları Güncelle
        if (bedenIds != null && stoklar != null) {
            List<UrunStok> eskiStoklar = urunStokRepository.findByUrunId(id);
            urunStokRepository.deleteAll(eskiStoklar);

            for (int i = 0; i < bedenIds.size(); i++) {
                if (i < stoklar.size() && stoklar.get(i) != null && stoklar.get(i) > 0) {
                    try {
                        Beden beden = bedenRepository.findById(bedenIds.get(i)).orElse(null);
                        if (beden != null) {
                            UrunStok stok = new UrunStok();
                            stok.setUrun(urun);
                            stok.setBeden(beden);
                            stok.setAdet(stoklar.get(i));
                            urunStokRepository.save(stok);
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }

        redirectAttributes.addFlashAttribute("basari", "Ürün başarıyla güncellendi!");
        return "redirect:/admin/magaza/" + urun.getMagaza().getId() + "/urunler";
    }

    // Ürün Pasife Al (Admin) - Soft Delete
    @PostMapping("/urun/{id}/pasif")
    @Transactional
    public String adminUrunPasifYap(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Urun urun = urunRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));
        Long magazaId = urun.getMagaza().getId();

        urun.setAktif(false);
        urunRepository.save(urun);

        redirectAttributes.addFlashAttribute("basari", "Ürün pasife alındı.");
        return "redirect:/admin/magaza/" + magazaId + "/urunler";
    }

    // Ürün Aktife Al (Admin)
    @PostMapping("/urun/{id}/aktif")
    public String adminUrunAktifYap(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Urun urun = urunRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));
        Long magazaId = urun.getMagaza().getId();

        urun.setAktif(true);
        urunRepository.save(urun);

        redirectAttributes.addFlashAttribute("basari", "Ürün aktife alındı.");
        return "redirect:/admin/magaza/" + magazaId + "/urunler";
    }

    // Ürün Kalıcı Sil (Admin) - Hard Delete
    @PostMapping("/urun/{id}/sil")
    @Transactional
    public String adminUrunKaliciSil(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Urun urun = urunRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));
        Long magazaId = urun.getMagaza().getId();

        // Önce stokları sil
        urunStokRepository.deleteByUrunId(id);
        // Sonra ürünü sil
        urunRepository.delete(urun);

        redirectAttributes.addFlashAttribute("basari", "Ürün kalıcı olarak silindi.");
        return "redirect:/admin/magaza/" + magazaId + "/urunler";
    }

    // ============ MESAJLAR ============
    @GetMapping("/mesajlar")
    public String mesajlar(Model model, org.springframework.security.core.Authentication auth) {
        List<Mesaj> tumMesajlar = mesajRepository.findAllByOrderByTarihDesc();

        Map<String, SohbetDTO> sohbetlerMap = new LinkedHashMap<>();

        for (Mesaj m : tumMesajlar) {
            if (m.getMusteri() == null || m.getMagaza() == null)
                continue;

            String key = m.getMagaza().getId() + "-" + m.getMusteri().getId();
            if (!sohbetlerMap.containsKey(key)) {
                sohbetlerMap.put(key, new SohbetDTO(m.getMagaza(), m.getMusteri(), m));
            }
        }

        model.addAttribute("sohbetler", new ArrayList<>(sohbetlerMap.values()));

        // Navbar için giriş yapan admin
        Kullanici admin = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Admin bulunamadı"));
        model.addAttribute("kullanici", admin);

        return "admin/mesajlar";
    }

    @GetMapping("/sohbet/magaza/{magazaId}/musteri/{musteriId}")
    public String adminSohbet(@PathVariable Long magazaId,
            @PathVariable Long musteriId,
            Model model,
            org.springframework.security.core.Authentication auth) {
        Magaza magaza = magazaRepository.findById(magazaId)
                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));
        Kullanici musteri = kullaniciRepository.findById(musteriId)
                .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı"));

        List<Mesaj> sohbetGecmisi = mesajRepository.findByMagazaIdAndMusteriIdOrderByTarihAsc(magazaId, musteriId);

        model.addAttribute("magaza", magaza);
        model.addAttribute("musteri", musteri);
        model.addAttribute("sohbetGecmisi", sohbetGecmisi);

        // Navbar için giriş yapan admin
        Kullanici admin = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Admin bulunamadı"));
        model.addAttribute("kullanici", admin);

        return "admin/sohbet-detay";
    }

    @PostMapping("/sohbet/gonder")
    public String adminMesajGonder(@RequestParam Long magazaId,
            @RequestParam Long musteriId,
            @RequestParam String icerik,
            @RequestParam String gonderenTipi, // "MAGAZA" veya "MUSTERI"
            RedirectAttributes redirectAttributes) {

        try {
            Magaza magaza = magazaRepository.findById(magazaId).orElseThrow();
            Kullanici musteri = kullaniciRepository.findById(musteriId).orElseThrow();

            Mesaj mesaj = new Mesaj();
            mesaj.setMagaza(magaza);
            mesaj.setMusteri(musteri);
            mesaj.setIcerik(icerik);

            if ("MAGAZA".equals(gonderenTipi)) {
                mesaj.setGonderen(magaza.getSahip());
                mesaj.setGonderenMusteri(false);
            } else {
                mesaj.setGonderen(musteri);
                mesaj.setGonderenMusteri(true);
            }
            mesaj.setOkundu(false);

            mesajRepository.save(mesaj);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Mesaj gönderilemedi: " + e.getMessage());
        }

        return "redirect:/admin/sohbet/magaza/" + magazaId + "/musteri/" + musteriId;
    }

    public static class SohbetDTO {
        private Magaza magaza;
        private Kullanici musteri;
        private Mesaj sonMesaj;

        public SohbetDTO(Magaza magaza, Kullanici musteri, Mesaj sonMesaj) {
            this.magaza = magaza;
            this.musteri = musteri;
            this.sonMesaj = sonMesaj;
        }

        public Magaza getMagaza() {
            return magaza;
        }

        public Kullanici getMusteri() {
            return musteri;
        }

        public Mesaj getSonMesaj() {
            return sonMesaj;
        }
    }
}
