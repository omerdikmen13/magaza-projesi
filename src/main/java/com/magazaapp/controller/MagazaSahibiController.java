package com.magazaapp.controller;

import com.magazaapp.model.*;
import com.magazaapp.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/sahip")
public class MagazaSahibiController {

        private final MagazaService magazaService;
        private final KullaniciService kullaniciService;
        private final UrunService urunService;
        private final SiparisService siparisService;
        private final KategoriService kategoriService;
        private final BedenService bedenService;
        private final AltKategoriService altKategoriService;

        public MagazaSahibiController(MagazaService magazaService, KullaniciService kullaniciService,
                        UrunService urunService, SiparisService siparisService,
                        KategoriService kategoriService, BedenService bedenService,
                        AltKategoriService altKategoriService) {
                this.magazaService = magazaService;
                this.kullaniciService = kullaniciService;
                this.urunService = urunService;
                this.siparisService = siparisService;
                this.kategoriService = kategoriService;
                this.bedenService = bedenService;
                this.altKategoriService = altKategoriService;
        }

        // ============ PANEL ANA SAYFA ============
        @GetMapping
        public String panel(Authentication auth, Model model) {
                Kullanici sahip = kullaniciService.getByUsername(auth.getName());
                List<Magaza> magazalar = magazaService.getMagazalarBySahip(sahip.getId());

                model.addAttribute("kullanici", sahip);
                model.addAttribute("magazalar", magazalar);

                return "sahip/panel";
        }

        // ============ YENİ MAĞAZA FORMU ============
        @GetMapping("/magaza/yeni")
        public String magazaOlusturForm(Model model) {
                model.addAttribute("magaza", new Magaza());
                return "sahip/magaza-olustur";
        }

        // ============ YENİ MAĞAZA KAYDET ============
        @PostMapping("/magaza/olustur")
        public String magazaOlustur(@RequestParam String ad,
                        @RequestParam String aciklama,
                        Authentication auth,
                        RedirectAttributes redirectAttributes) {
                try {
                        Kullanici sahip = kullaniciService.getByUsername(auth.getName());

                        Magaza magaza = new Magaza();
                        magaza.setAd(ad);
                        magaza.setAciklama(aciklama);
                        magaza.setSahip(sahip);
                        magaza.setAktif(true);

                        magazaService.kaydet(magaza);
                        redirectAttributes.addFlashAttribute("basari", "Mağaza başarıyla oluşturuldu!");
                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("hata", "Mağaza oluşturulamadı: " + e.getMessage());
                }
                return "redirect:/sahip";
        }

        // ============ MAĞAZA DÜZENLEME FORMU ============
        @GetMapping("/magaza/{id}/duzenle")
        public String magazaDuzenleForm(@PathVariable Long id, Authentication auth, Model model) {
                Kullanici sahip = kullaniciService.getByUsername(auth.getName());
                Magaza magaza = magazaService.getMagazaById(id);

                // Yetki kontrolü
                if (!magaza.getSahip().getId().equals(sahip.getId())) {
                        return "redirect:/sahip";
                }

                model.addAttribute("kullanici", sahip);
                model.addAttribute("magaza", magaza);
                return "sahip/magaza-duzenle";
        }

        // ============ MAĞAZA GÜNCELLE ============
        @PostMapping("/magaza/{id}/duzenle")
        public String magazaDuzenle(@PathVariable Long id,
                        @RequestParam String ad,
                        @RequestParam String aciklama,
                        @RequestParam(required = false) String logoUrl,
                        Authentication auth,
                        RedirectAttributes redirectAttributes) {
                try {
                        Kullanici sahip = kullaniciService.getByUsername(auth.getName());
                        Magaza magaza = magazaService.getMagazaById(id);

                        if (!magaza.getSahip().getId().equals(sahip.getId())) {
                                redirectAttributes.addFlashAttribute("hata", "Bu mağazayı düzenleme yetkiniz yok!");
                                return "redirect:/sahip";
                        }

                        magaza.setAd(ad);
                        magaza.setAciklama(aciklama);
                        if (logoUrl != null && !logoUrl.isEmpty()) {
                                magaza.setLogoUrl(logoUrl);
                        }
                        magazaService.kaydet(magaza);

                        redirectAttributes.addFlashAttribute("basari", "Mağaza bilgileri güncellendi!");
                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("hata", "Güncelleme hatası: " + e.getMessage());
                }
                return "redirect:/sahip";
        }

        // ============ MAĞAZA SİL (PASİFE AL) ============
        @PostMapping("/magaza/{id}/sil")
        public String magazaSil(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
                try {
                        Kullanici sahip = kullaniciService.getByUsername(auth.getName());
                        Magaza magaza = magazaService.getMagazaById(id);

                        if (!magaza.getSahip().getId().equals(sahip.getId())) {
                                redirectAttributes.addFlashAttribute("hata", "Bu mağazayı silme yetkiniz yok!");
                                return "redirect:/sahip";
                        }

                        magaza.setAktif(false);
                        magazaService.kaydet(magaza);
                        redirectAttributes.addFlashAttribute("basari", "Mağaza pasife alındı.");
                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("hata", "Silme hatası: " + e.getMessage());
                }
                return "redirect:/sahip";
        }

        // ============ MAĞAZA YÖNETİMİ ============
        @GetMapping("/magaza/{id}")
        public String magazaYonetim(@PathVariable Long id, Authentication auth, Model model) {
                Kullanici sahip = kullaniciService.getByUsername(auth.getName());
                Magaza magaza = magazaService.getMagazaById(id);

                if (!magaza.getSahip().getId().equals(sahip.getId())) {
                        return "redirect:/sahip";
                }

                List<Urun> urunler = magazaService.getMagazaninUrunleri(id);

                model.addAttribute("kullanici", sahip);
                model.addAttribute("magaza", magaza);
                model.addAttribute("urunler", urunler);

                return "sahip/magaza-yonetim";
        }

        // ============ ÜRÜN EKLEME FORMU ============
        @GetMapping({ "/magaza/{id}/urun/ekle", "/magaza/{id}/urun-ekle" })
        public String urunEkleForm(@PathVariable Long id, Authentication auth, Model model) {
                Kullanici sahip = kullaniciService.getByUsername(auth.getName());
                Magaza magaza = magazaService.getMagazaById(id);

                if (!magaza.getSahip().getId().equals(sahip.getId())) {
                        return "redirect:/sahip";
                }

                List<Kategori> kategoriler = kategoriService.getTumKategoriler();
                List<AltKategori> altKategoriler = altKategoriService.getTumAltKategoriler();
                List<Beden> bedenler = bedenService.getTumBedenler();

                model.addAttribute("kullanici", sahip);
                model.addAttribute("magaza", magaza);
                model.addAttribute("kategoriler", kategoriler);
                model.addAttribute("altKategoriler", altKategoriler);
                model.addAttribute("bedenler", bedenler);

                return "sahip/urun-ekle";
        }

        // ============ ÜRÜN KAYDET ============
        @PostMapping({ "/magaza/{id}/urun/ekle", "/magaza/{id}/urun-ekle" })
        public String urunEkle(@PathVariable Long id,
                        @RequestParam String ad,
                        @RequestParam String aciklama,
                        @RequestParam BigDecimal fiyat,
                        @RequestParam String renk,
                        @RequestParam(required = false) String resimUrl,
                        @RequestParam Long altKategoriId,
                        @RequestParam List<Long> bedenIds,
                        @RequestParam List<Integer> stoklar,
                        Authentication auth,
                        RedirectAttributes redirectAttributes) {
                try {
                        Kullanici sahip = kullaniciService.getByUsername(auth.getName());
                        Magaza magaza = magazaService.getMagazaById(id);

                        if (!magaza.getSahip().getId().equals(sahip.getId())) {
                                redirectAttributes.addFlashAttribute("hata", "Bu mağazaya ürün ekleme yetkiniz yok!");
                                return "redirect:/sahip";
                        }

                        // Ürün oluştur ve kaydet
                        Urun urun = new Urun();
                        urun.setAd(ad);
                        urun.setAciklama(aciklama);
                        urun.setFiyat(fiyat);
                        urun.setRenk(renk);
                        urun.setResimUrl(resimUrl);
                        urun.setMagaza(magaza);
                        urun.setAktif(true);

                        // AltKategori ayarla
                        AltKategori altKategori = kategoriService.getAltKategoriById(altKategoriId);
                        urun.setAltKategori(altKategori);

                        urun = urunService.saveUrun(urun);

                        // Stokları kaydet
                        for (int i = 0; i < bedenIds.size(); i++) {
                                if (i < stoklar.size() && stoklar.get(i) > 0) {
                                        Beden beden = bedenService.getBedenById(bedenIds.get(i));
                                        urunService.updateStok(urun.getId(), beden.getId(), stoklar.get(i));
                                }
                        }

                        redirectAttributes.addFlashAttribute("basari", "Ürün başarıyla eklendi!");
                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("hata", "Ürün eklenemedi: " + e.getMessage());
                }
                return "redirect:/sahip/magaza/" + id;
        }

        // ============ ÜRÜN DÜZENLEME FORMU ============
        @GetMapping({ "/magaza/{magazaId}/urun/{urunId}/duzenle", "/magaza/{magazaId}/urun-duzenle/{urunId}" })
        public String urunDuzenleForm(@PathVariable Long magazaId, @PathVariable Long urunId,
                        Authentication auth, Model model) {
                Kullanici sahip = kullaniciService.getByUsername(auth.getName());
                Magaza magaza = magazaService.getMagazaById(magazaId);

                if (!magaza.getSahip().getId().equals(sahip.getId())) {
                        return "redirect:/sahip";
                }

                Urun urun = urunService.getUrunById(urunId);
                List<Kategori> kategoriler = kategoriService.getTumKategoriler();
                List<AltKategori> altKategoriler = altKategoriService.getTumAltKategoriler();
                List<Beden> bedenler = bedenService.getTumBedenler();
                List<UrunStok> mevcutStoklar = urunService.getUrunStoklari(urunId);

                model.addAttribute("kullanici", sahip);
                model.addAttribute("magaza", magaza);
                model.addAttribute("urun", urun);
                model.addAttribute("kategoriler", kategoriler);
                model.addAttribute("altKategoriler", altKategoriler);
                model.addAttribute("bedenler", bedenler);
                model.addAttribute("mevcutStoklar", mevcutStoklar);

                return "sahip/urun-duzenle";
        }

        // ============ ÜRÜN DÜZENLE KAYDET ============
        @PostMapping({ "/magaza/{magazaId}/urun/{urunId}/duzenle", "/magaza/{magazaId}/urun-duzenle/{urunId}" })
        public String urunDuzenle(@PathVariable Long magazaId,
                        @PathVariable Long urunId,
                        @RequestParam String ad,
                        @RequestParam String aciklama,
                        @RequestParam BigDecimal fiyat,
                        @RequestParam String renk,
                        @RequestParam(required = false) String resimUrl,
                        @RequestParam Long altKategoriId,
                        @RequestParam(required = false) List<Long> bedenIds,
                        @RequestParam(required = false) List<Integer> stoklar,
                        Authentication auth,
                        RedirectAttributes redirectAttributes) {
                try {
                        Kullanici sahip = kullaniciService.getByUsername(auth.getName());
                        Magaza magaza = magazaService.getMagazaById(magazaId);

                        if (!magaza.getSahip().getId().equals(sahip.getId())) {
                                redirectAttributes.addFlashAttribute("hata", "Bu ürünü düzenleme yetkiniz yok!");
                                return "redirect:/sahip";
                        }

                        Urun urun = urunService.getUrunById(urunId);
                        urun.setAd(ad);
                        urun.setAciklama(aciklama);
                        urun.setFiyat(fiyat);
                        urun.setRenk(renk);
                        if (resimUrl != null && !resimUrl.isEmpty()) {
                                urun.setResimUrl(resimUrl);
                        }

                        AltKategori altKategori = kategoriService.getAltKategoriById(altKategoriId);
                        urun.setAltKategori(altKategori);

                        urunService.saveUrun(urun);

                        // Stokları güncelle
                        if (bedenIds != null && stoklar != null) {
                                for (int i = 0; i < bedenIds.size(); i++) {
                                        if (i < stoklar.size()) {
                                                urunService.updateStok(urunId, bedenIds.get(i), stoklar.get(i));
                                        }
                                }
                        }

                        redirectAttributes.addFlashAttribute("basari", "Ürün güncellendi!");
                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("hata", "Güncelleme hatası: " + e.getMessage());
                }
                return "redirect:/sahip/magaza/" + magazaId;
        }

        // ============ ÜRÜN PASİFE AL ============
        @PostMapping("/urun/{id}/pasif")
        public String urunPasifYap(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
                try {
                        Urun urun = urunService.getUrunById(id);
                        Kullanici sahip = kullaniciService.getByUsername(auth.getName());

                        if (!urun.getMagaza().getSahip().getId().equals(sahip.getId())) {
                                redirectAttributes.addFlashAttribute("hata", "Bu ürünü düzenleme yetkiniz yok!");
                                return "redirect:/sahip";
                        }

                        urunService.deleteUrun(id); // Soft delete
                        redirectAttributes.addFlashAttribute("basari", "Ürün pasife alındı.");
                        return "redirect:/sahip/magaza/" + urun.getMagaza().getId();
                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("hata", "Hata: " + e.getMessage());
                        return "redirect:/sahip";
                }
        }

        // ============ ÜRÜN AKTİFE AL ============
        @PostMapping("/urun/{id}/aktif")
        public String urunAktifYap(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
                try {
                        Urun urun = urunService.getUrunById(id);
                        Kullanici sahip = kullaniciService.getByUsername(auth.getName());

                        if (!urun.getMagaza().getSahip().getId().equals(sahip.getId())) {
                                redirectAttributes.addFlashAttribute("hata", "Bu ürünü düzenleme yetkiniz yok!");
                                return "redirect:/sahip";
                        }

                        urun.setAktif(true);
                        urunService.saveUrun(urun);
                        redirectAttributes.addFlashAttribute("basari", "Ürün aktife alındı.");
                        return "redirect:/sahip/magaza/" + urun.getMagaza().getId();
                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("hata", "Hata: " + e.getMessage());
                        return "redirect:/sahip";
                }
        }

        // ============ CİRO GÖRÜNTÜLEME ============
        @GetMapping("/magaza/{id}/ciro")
        public String magazaCiro(@PathVariable Long id, Authentication auth, Model model) {
                Kullanici sahip = kullaniciService.getByUsername(auth.getName());
                Magaza magaza = magazaService.getMagazaById(id);

                if (!magaza.getSahip().getId().equals(sahip.getId())) {
                        return "redirect:/sahip";
                }

                List<SiparisFisi> siparisler = siparisService.getMagazaSiparisleri(id);

                // Ciro hesapla
                BigDecimal toplamCiro = siparisler.stream()
                                .filter(s -> s.getDurum() == SiparisDurum.TESLIM_EDILDI)
                                .map(s -> s.getToplamTutar() != null ? s.getToplamTutar() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal bekleyenCiro = siparisler.stream()
                                .filter(s -> s.getDurum() != SiparisDurum.TESLIM_EDILDI
                                                && s.getDurum() != SiparisDurum.IPTAL)
                                .map(s -> s.getToplamTutar() != null ? s.getToplamTutar() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                long teslimEdilen = siparisler.stream()
                                .filter(s -> s.getDurum() == SiparisDurum.TESLIM_EDILDI).count();
                long bekleyen = siparisler.stream()
                                .filter(s -> s.getDurum() != SiparisDurum.TESLIM_EDILDI
                                                && s.getDurum() != SiparisDurum.IPTAL)
                                .count();

                model.addAttribute("kullanici", sahip);
                model.addAttribute("magaza", magaza);
                model.addAttribute("toplamCiro", toplamCiro);
                model.addAttribute("bekleyenCiro", bekleyenCiro);
                model.addAttribute("teslimEdilen", teslimEdilen);
                model.addAttribute("bekleyenSayisi", bekleyen);
                model.addAttribute("siparisler", siparisler);

                return "sahip/ciro";
        }

        // ============ SİPARİŞ YÖNETİMİ ============
        @GetMapping("/magaza/{id}/siparisler")
        public String magazaSiparisler(@PathVariable Long id, Authentication auth, Model model) {
                Kullanici sahip = kullaniciService.getByUsername(auth.getName());
                Magaza magaza = magazaService.getMagazaById(id);

                if (!magaza.getSahip().getId().equals(sahip.getId())) {
                        return "redirect:/sahip";
                }

                List<SiparisFisi> siparisler = siparisService.getMagazaSiparisleri(id);

                // İstatistikler
                long bekleyenSayisi = siparisler.stream()
                                .filter(s -> s.getDurum() == SiparisDurum.BEKLEMEDE).count();
                long hazirlaniyor = siparisler.stream()
                                .filter(s -> s.getDurum() == SiparisDurum.HAZIRLANIYOR).count();
                long kargoSayisi = siparisler.stream()
                                .filter(s -> s.getDurum() == SiparisDurum.KARGODA).count();

                BigDecimal toplamCiro = siparisler.stream()
                                .filter(s -> s.getDurum() == SiparisDurum.TESLIM_EDILDI)
                                .map(s -> s.getToplamTutar() != null ? s.getToplamTutar() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                model.addAttribute("kullanici", sahip);
                model.addAttribute("magaza", magaza);
                model.addAttribute("siparisler", siparisler);
                model.addAttribute("bekleyenSayisi", bekleyenSayisi);
                model.addAttribute("hazirlaniyor", hazirlaniyor);
                model.addAttribute("kargoSayisi", kargoSayisi);
                model.addAttribute("toplamCiro", toplamCiro);

                return "sahip/siparisler";
        }

        // ============ SİPARİŞ DURUMU GÜNCELLE ============
        @PostMapping({ "/siparis/{id}/durum", "/siparis/{id}/guncelle", "/siparis/{id}/durum-guncelle" })
        public String siparisDurumuGuncelle(@PathVariable Long id,
                        @RequestParam String durum,
                        Authentication auth,
                        RedirectAttributes redirectAttributes) {
                try {
                        SiparisFisi siparis = siparisService.getSiparisById(id);
                        Kullanici sahip = kullaniciService.getByUsername(auth.getName());

                        if (!siparis.getMagaza().getSahip().getId().equals(sahip.getId())) {
                                redirectAttributes.addFlashAttribute("hata", "Bu siparişi güncelleme yetkiniz yok!");
                                return "redirect:/sahip";
                        }

                        SiparisDurum yeniDurum = SiparisDurum.valueOf(durum);
                        siparisService.siparisDurumGuncelle(id, yeniDurum);

                        redirectAttributes.addFlashAttribute("basari", "Sipariş durumu güncellendi.");
                        return "redirect:/sahip/magaza/" + siparis.getMagaza().getId() + "/siparisler";
                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("hata", "Güncelleme hatası: " + e.getMessage());
                        return "redirect:/sahip";
                }
        }

        // ============ SİPARİŞ DETAY ============
        @GetMapping("/siparis/{id}")
        public String siparisDetay(@PathVariable Long id, Authentication auth, Model model) {
                Kullanici sahip = kullaniciService.getByUsername(auth.getName());
                SiparisFisi siparis = siparisService.getSiparisById(id);

                if (!siparis.getMagaza().getSahip().getId().equals(sahip.getId())) {
                        return "redirect:/sahip";
                }

                List<SiparisDetay> detaylar = siparisService.getSiparisDetaylari(id);

                model.addAttribute("kullanici", sahip);
                model.addAttribute("siparis", siparis);
                model.addAttribute("detaylar", detaylar);

                return "sahip/siparis-detay";
        }

        // ============ SİPARİŞ DÜZENLEME SAYFASI ============
        @GetMapping("/siparis/{id}/duzenle")
        public String siparisDuzenleForm(@PathVariable Long id, Authentication auth, Model model) {
                Kullanici sahip = kullaniciService.getByUsername(auth.getName());
                SiparisFisi siparis = siparisService.getSiparisById(id);

                if (!siparis.getMagaza().getSahip().getId().equals(sahip.getId())) {
                        return "redirect:/sahip";
                }

                List<SiparisDetay> detaylar = siparisService.getSiparisDetaylari(id);

                model.addAttribute("kullanici", sahip);
                model.addAttribute("magaza", siparis.getMagaza());
                model.addAttribute("siparis", siparis);
                model.addAttribute("detaylar", detaylar);
                model.addAttribute("durumlar", SiparisDurum.values());

                return "sahip/siparis-duzenle";
        }
}
