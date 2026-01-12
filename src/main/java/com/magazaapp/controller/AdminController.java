package com.magazaapp.controller;

import com.magazaapp.model.*;
import com.magazaapp.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final KullaniciService kullaniciService;
    private final MagazaService magazaService;
    private final UrunService urunService;
    private final SiparisService siparisService;
    private final MesajService mesajService;
    private final KategoriService kategoriService;
    private final BedenService bedenService;

    public AdminController(KullaniciService kullaniciService, MagazaService magazaService,
            UrunService urunService, SiparisService siparisService,
            MesajService mesajService, KategoriService kategoriService,
            BedenService bedenService) {
        this.kullaniciService = kullaniciService;
        this.magazaService = magazaService;
        this.urunService = urunService;
        this.siparisService = siparisService;
        this.mesajService = mesajService;
        this.kategoriService = kategoriService;
        this.bedenService = bedenService;
    }

    // ============ DASHBOARD ============
    @GetMapping
    public String dashboard(Model model) {
        // Service'ler üzerinden istatistikleri al
        long toplamKullanici = kullaniciService.getTumKullanicilar().size();
        long toplamMagaza = magazaService.getTumMagazalar().size();
        long toplamUrun = urunService.getAktifUrunler().size();

        List<SiparisFisi> tumSiparisler = siparisService.getTumSiparisler();
        long toplamSiparis = tumSiparisler.size();
        long bekleyenSiparis = tumSiparisler.stream()
                .filter(s -> s.getDurum() == SiparisDurum.BEKLEMEDE).count();

        BigDecimal toplamCiro = tumSiparisler.stream()
                .filter(s -> s.getDurum() != SiparisDurum.IPTAL)
                .map(s -> s.getToplamTutar() != null ? s.getToplamTutar() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SiparisFisi> sonSiparisler = tumSiparisler.stream()
                .sorted((a, b) -> b.getSiparisTarihi().compareTo(a.getSiparisTarihi()))
                .limit(5).toList();

        List<Kullanici> sonKullanicilar = kullaniciService.getTumKullanicilar().stream()
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
        List<Kullanici> kullanicilar = kullaniciService.getTumKullanicilar();
        model.addAttribute("kullanicilar", kullanicilar);
        return "admin/kullanicilar";
    }

    @PostMapping("/kullanici/{id}/rol-degistir")
    public String rolDegistir(@PathVariable Long id, @RequestParam String yeniRol,
            RedirectAttributes redirectAttributes) {
        try {
            Kullanici kullanici = kullaniciService.getKullaniciById(id);
            KullaniciRol rol = KullaniciRol.valueOf(yeniRol);
            kullanici.setRol(rol);
            kullaniciService.kaydet(kullanici);
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
            Kullanici kullanici = kullaniciService.getKullaniciById(id);

            if (kullanici.getRol() == KullaniciRol.ADMIN) {
                redirectAttributes.addFlashAttribute("hata", "Admin kullanıcıları silinemez!");
                return "redirect:/admin/kullanicilar";
            }

            // Mağaza sahibi kontrolü
            if (kullanici.getRol() == KullaniciRol.MAGAZA_SAHIBI) {
                List<Magaza> magazalar = magazaService.getMagazalarBySahip(id);
                if (!magazalar.isEmpty()) {
                    redirectAttributes.addFlashAttribute("hata",
                            "Bu kullanıcının " + magazalar.size() + " mağazası var! Önce mağazaları silin.");
                    return "redirect:/admin/kullanicilar";
                }
            }

            String kullaniciAdi = kullanici.getKullaniciAdi();
            kullaniciService.sil(id);
            redirectAttributes.addFlashAttribute("basari", kullaniciAdi + " kullanıcısı silindi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Silme hatası: " + e.getMessage());
        }
        return "redirect:/admin/kullanicilar";
    }

    @GetMapping("/kullanici/{id}/duzenle")
    public String kullaniciDuzenleForm(@PathVariable Long id, Model model, Authentication auth) {
        Kullanici duzenlenecek = kullaniciService.getKullaniciById(id);
        model.addAttribute("duzenlenecekKullanici", duzenlenecek);

        Kullanici admin = kullaniciService.getByUsername(auth.getName());
        model.addAttribute("kullanici", admin);

        return "admin/kullanici-duzenle";
    }

    @PostMapping("/kullanici/{id}/duzenle")
    public String kullaniciDuzenle(@PathVariable Long id, @RequestParam String ad, @RequestParam String soyad,
            @RequestParam String email, @RequestParam String telefon,
            RedirectAttributes redirectAttributes) {
        try {
            kullaniciService.updateProfile(id, ad, soyad, email, telefon, null);
            redirectAttributes.addFlashAttribute("basari", "Kullanıcı bilgileri güncellendi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", e.getMessage());
        }
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
            kullaniciService.registerUser(kullaniciAdi, email, sifre, ad, soyad, KullaniciRol.valueOf(rol));
            redirectAttributes.addFlashAttribute("basari", "Kullanıcı başarıyla oluşturuldu.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Kullanıcı oluşturma hatası: " + e.getMessage());
        }
        return "redirect:/admin/kullanicilar";
    }

    // ============ MAĞAZA YÖNETİMİ ============
    @GetMapping("/magazalar")
    public String magazaListesi(Model model) {
        List<Magaza> magazalar = magazaService.getTumMagazalar();
        model.addAttribute("magazalar", magazalar);
        return "admin/magazalar";
    }

    @PostMapping("/magaza/{id}/durum-degistir")
    public String magazaDurumDegistir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Magaza magaza = magazaService.getMagazaById(id);
            magaza.setAktif(!magaza.getAktif());
            magazaService.kaydet(magaza);

            String durum = magaza.getAktif() ? "aktif" : "pasif";
            redirectAttributes.addFlashAttribute("basari", magaza.getAd() + " mağazası " + durum + " yapıldı.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Durum değiştirme hatası: " + e.getMessage());
        }
        return "redirect:/admin/magazalar";
    }

    @PostMapping("/magaza/{id}/sil")
    public String magazaSil(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Magaza magaza = magazaService.getMagazaById(id);
            String magazaAd = magaza.getAd();
            magazaService.sil(id);
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
        List<SiparisFisi> siparisler = siparisService.getTumSiparisler();

        if (durum != null && !durum.isEmpty()) {
            try {
                SiparisDurum siparisDurum = SiparisDurum.valueOf(durum);
                siparisler = siparisler.stream()
                        .filter(s -> s.getDurum() == siparisDurum).toList();
            } catch (Exception e) {
                // ignore
            }
        }

        if (magazaId != null) {
            siparisler = siparisler.stream()
                    .filter(s -> s.getMagaza() != null && s.getMagaza().getId().equals(magazaId))
                    .toList();
            model.addAttribute("filtreMagaza", magazaService.getMagazaById(magazaId));
        }

        model.addAttribute("siparisler", siparisler);
        model.addAttribute("seciliDurum", durum);
        model.addAttribute("durumlar", SiparisDurum.values());

        return "admin/siparisler";
    }

    @PostMapping("/siparis/{id}/durum-guncelle")
    public String siparisDurumGuncelle(@PathVariable Long id, @RequestParam String yeniDurum,
            RedirectAttributes redirectAttributes) {
        try {
            SiparisDurum durum = SiparisDurum.valueOf(yeniDurum);
            siparisService.siparisDurumGuncelle(id, durum);
            redirectAttributes.addFlashAttribute("basari",
                    "Sipariş #" + id + " durumu '" + durum + "' olarak güncellendi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Durum güncelleme hatası: " + e.getMessage());
        }
        return "redirect:/admin/siparisler";
    }

    @GetMapping("/siparis/{id}/duzenle")
    public String siparisDuzenleForm(@PathVariable Long id, Model model, Authentication auth) {
        SiparisFisi siparis = siparisService.getSiparisById(id);
        List<SiparisDetay> siparisDetaylari = siparisService.getSiparisDetaylari(id);

        model.addAttribute("siparis", siparis);
        model.addAttribute("siparisDetaylari", siparisDetaylari);
        model.addAttribute("durumlar", SiparisDurum.values());

        Kullanici admin = kullaniciService.getByUsername(auth.getName());
        model.addAttribute("kullanici", admin);

        return "admin/siparis-duzenle";
    }

    @PostMapping("/siparis/{id}/duzenle")
    public String siparisDuzenle(@PathVariable Long id,
            @RequestParam SiparisDurum durum,
            @RequestParam String teslimatAdresi,
            RedirectAttributes redirectAttributes) {
        SiparisFisi siparis = siparisService.getSiparisById(id);
        siparis.setDurum(durum);
        siparis.setTeslimatAdresi(teslimatAdresi);
        // Service'de save metodu gerekebilir
        siparisService.siparisDurumGuncelle(id, durum);

        redirectAttributes.addFlashAttribute("basari", "Sipariş #" + id + " bilgileri güncellendi.");
        return "redirect:/admin/siparisler";
    }

    // ============ CİRO RAPORU ============
    @GetMapping("/ciro")
    public String ciroRaporu(@RequestParam(required = false, defaultValue = "TUMU") String donem, Model model) {
        java.time.LocalDateTime baslangicTarihi;
        java.time.LocalDateTime simdi = java.time.LocalDateTime.now();

        if ("HAFTALIK".equals(donem)) {
            baslangicTarihi = simdi.minusWeeks(1);
        } else if ("AYLIK".equals(donem)) {
            baslangicTarihi = simdi.minusMonths(1);
        } else if ("YILLIK".equals(donem)) {
            baslangicTarihi = simdi.minusYears(1);
        } else {
            baslangicTarihi = java.time.LocalDateTime.of(2000, 1, 1, 0, 0);
        }

        List<SiparisFisi> tumSiparisler = siparisService.getTumSiparisler().stream()
                .filter(s -> s.getSiparisTarihi().isAfter(baslangicTarihi))
                .toList();

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

        // Mağaza Bazlı Rapor
        List<Magaza> magazalar = magazaService.getTumMagazalar();
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

        magazaRaporlari.sort((r1, r2) -> ((BigDecimal) r2.get("ciro")).compareTo((BigDecimal) r1.get("ciro")));

        model.addAttribute("toplamCiro", toplamCiro);
        model.addAttribute("tamamlananCiro", tamamlananCiro);
        model.addAttribute("bekleyenCiro", bekleyenCiro);
        model.addAttribute("magazaRaporlari", magazaRaporlari);
        model.addAttribute("seciliDonem", donem);

        return "admin/ciro";
    }

    // ============ MAĞAZA DÜZENLEME ============
    @GetMapping("/magaza/{id}/duzenle")
    public String magazaDuzenleForm(@PathVariable Long id, Model model, Authentication auth) {
        Magaza magaza = magazaService.getMagazaById(id);
        model.addAttribute("magaza", magaza);

        Kullanici admin = kullaniciService.getByUsername(auth.getName());
        model.addAttribute("kullanici", admin);

        return "admin/magaza-duzenle";
    }

    @PostMapping("/magaza/{id}/duzenle")
    public String magazaDuzenle(@PathVariable Long id, @RequestParam String ad, @RequestParam String aciklama,
            @RequestParam String logoUrl, RedirectAttributes redirectAttributes) {
        Magaza magaza = magazaService.getMagazaById(id);
        magaza.setAd(ad);
        magaza.setAciklama(aciklama);
        magaza.setLogoUrl(logoUrl);
        magazaService.kaydet(magaza);

        redirectAttributes.addFlashAttribute("basari", "Mağaza bilgileri güncellendi.");
        return "redirect:/admin/magazalar";
    }

    @GetMapping("/magaza/{id}/urunler")
    public String magazaUrunleri(@PathVariable Long id, Model model) {
        Magaza magaza = magazaService.getMagazaById(id);
        List<Urun> urunler = magazaService.getMagazaninUrunleri(id);
        model.addAttribute("magaza", magaza);
        model.addAttribute("urunler", urunler);
        return "admin/magaza-urunler";
    }

    // ============ ÜRÜN YÖNETİMİ ============
    @GetMapping("/urun/{id}/duzenle")
    public String urunDuzenleForm(@PathVariable Long id, Model model, Authentication auth) {
        Urun urun = urunService.getUrunById(id);
        List<Kategori> kategoriler = kategoriService.getTumKategoriler();
        List<Beden> bedenler = bedenService.getTumBedenler();
        List<UrunStok> mevcutStoklar = urunService.getUrunStoklari(id);

        model.addAttribute("urun", urun);
        model.addAttribute("magaza", urun.getMagaza());
        model.addAttribute("kategoriler", kategoriler);
        model.addAttribute("bedenler", bedenler);
        model.addAttribute("mevcutStoklar", mevcutStoklar);

        Kullanici admin = kullaniciService.getByUsername(auth.getName());
        model.addAttribute("kullanici", admin);

        return "admin/urun-duzenle";
    }

    @PostMapping("/urun/{id}/pasif")
    public String adminUrunPasifYap(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Urun urun = urunService.getUrunById(id);
        Long magazaId = urun.getMagaza().getId();
        urunService.deleteUrun(id); // Soft delete
        redirectAttributes.addFlashAttribute("basari", "Ürün pasife alındı.");
        return "redirect:/admin/magaza/" + magazaId + "/urunler";
    }

    @PostMapping("/urun/{id}/aktif")
    public String adminUrunAktifYap(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Urun urun = urunService.getUrunById(id);
        Long magazaId = urun.getMagaza().getId();
        urun.setAktif(true);
        urunService.saveUrun(urun);
        redirectAttributes.addFlashAttribute("basari", "Ürün aktife alındı.");
        return "redirect:/admin/magaza/" + magazaId + "/urunler";
    }

    // ============ MESAJLAR ============
    @GetMapping("/mesajlar")
    public String mesajlar(Model model, Authentication auth) {
        List<Mesaj> tumMesajlar = mesajService.getTumMesajlar();

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
        Kullanici admin = kullaniciService.getByUsername(auth.getName());
        model.addAttribute("kullanici", admin);

        return "admin/mesajlar";
    }

    @GetMapping("/sohbet/magaza/{magazaId}/musteri/{musteriId}")
    public String adminSohbet(@PathVariable Long magazaId, @PathVariable Long musteriId,
            Model model, Authentication auth) {
        Magaza magaza = magazaService.getMagazaById(magazaId);
        Kullanici musteri = kullaniciService.getKullaniciById(musteriId);
        List<Mesaj> sohbetGecmisi = mesajService.getMagazaMusteriSohbet(magazaId, musteriId, false);

        model.addAttribute("magaza", magaza);
        model.addAttribute("musteri", musteri);
        model.addAttribute("sohbetGecmisi", sohbetGecmisi);

        Kullanici admin = kullaniciService.getByUsername(auth.getName());
        model.addAttribute("kullanici", admin);

        return "admin/sohbet-detay";
    }

    // Inner class for chat summary
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
