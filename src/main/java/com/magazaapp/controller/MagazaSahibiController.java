package com.magazaapp.controller;

import com.magazaapp.model.*;
import com.magazaapp.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/sahip")
public class MagazaSahibiController {

        private final MagazaRepository magazaRepository;
        private final KullaniciRepository kullaniciRepository;
        private final UrunRepository urunRepository;
        private final KategoriRepository kategoriRepository;
        private final AltKategoriRepository altKategoriRepository;
        private final BedenRepository bedenRepository;
        private final UrunStokRepository urunStokRepository;
        private final SiparisFisiRepository siparisFisiRepository;
        private final SiparisDetayRepository siparisDetayRepository;

        public MagazaSahibiController(MagazaRepository magazaRepository, KullaniciRepository kullaniciRepository,
                        UrunRepository urunRepository, KategoriRepository kategoriRepository,
                        AltKategoriRepository altKategoriRepository, BedenRepository bedenRepository,
                        UrunStokRepository urunStokRepository, SiparisFisiRepository siparisFisiRepository,
                        SiparisDetayRepository siparisDetayRepository) {
                this.magazaRepository = magazaRepository;
                this.kullaniciRepository = kullaniciRepository;
                this.urunRepository = urunRepository;
                this.kategoriRepository = kategoriRepository;
                this.altKategoriRepository = altKategoriRepository;
                this.bedenRepository = bedenRepository;
                this.urunStokRepository = urunStokRepository;
                this.siparisFisiRepository = siparisFisiRepository;
                this.siparisDetayRepository = siparisDetayRepository;
        }

        // ============ PANEL ANA SAYFA ============
        @GetMapping
        public String panel(Authentication auth, Model model) {
                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                // Kullanıcının mağazalarını bul
                List<Magaza> magazalar = magazaRepository.findBySahipId(kullanici.getId());

                model.addAttribute("kullanici", kullanici);
                model.addAttribute("magazalar", magazalar);

                return "sahip/panel";
        }

        // ============ YENİ MAĞAZA FORMU ============
        @GetMapping("/magaza-olustur")
        public String magazaOlusturForm(Model model) {
                model.addAttribute("magaza", new Magaza());
                return "sahip/magaza-olustur";
        }

        // ============ YENİ MAĞAZA KAYDET ============
        @PostMapping("/magaza-olustur")
        public String magazaOlustur(@RequestParam String ad,
                        @RequestParam String aciklama,
                        Authentication auth,
                        RedirectAttributes redirectAttributes) {

                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                // Kullanıcıyı mağaza sahibi yap
                if (kullanici.getRol() == KullaniciRol.MUSTERI) {
                        kullanici.setRol(KullaniciRol.MAGAZA_SAHIBI);
                        kullaniciRepository.save(kullanici);
                }

                Magaza magaza = new Magaza();
                magaza.setAd(ad);
                magaza.setAciklama(aciklama);
                magaza.setSahip(kullanici);
                magaza.setAktif(true);
                magazaRepository.save(magaza);

                redirectAttributes.addFlashAttribute("basari", "Mağaza başarıyla oluşturuldu!");
                return "redirect:/sahip";
        }

        // ============ MAĞAZA DÜZENLEME FORMU ============
        @GetMapping("/magaza/{id}/duzenle")
        public String magazaDuzenleForm(@PathVariable Long id, Authentication auth, Model model) {
                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                Magaza magaza = magazaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

                // Sahiplik kontrolü
                if (!magaza.getSahip().getId().equals(kullanici.getId())) {
                        return "redirect:/sahip";
                }

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

                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                Magaza magaza = magazaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

                if (!magaza.getSahip().getId().equals(kullanici.getId())) {
                        return "redirect:/sahip";
                }

                magaza.setAd(ad);
                magaza.setAciklama(aciklama);
                magaza.setLogoUrl(logoUrl);
                magazaRepository.save(magaza);

                redirectAttributes.addFlashAttribute("basari", "Mağaza bilgileri güncellendi!");
                return "redirect:/sahip";
        }

        // ============ MAĞAZA SİL (PASİFE AL) ============
        @PostMapping("/magaza/{id}/sil")
        public String magazaSil(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                Magaza magaza = magazaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

                if (!magaza.getSahip().getId().equals(kullanici.getId())) {
                        return "redirect:/sahip";
                }

                // Mağazayı tamamen silmek yerine pasife alıyoruz (veri kaybını önlemek için)
                // Eğer silmek istersek related kayıtları da silmek gerekir (ürünler, siparişler
                // vs)
                // Ancak kullanıcı "sil" dediği için burada aktifliğini false yapıyoruz.
                // İsteğe göre delete de atılabilir ama riskli.
                magaza.setAktif(false);
                magazaRepository.save(magaza);

                redirectAttributes.addFlashAttribute("basari", "Mağaza silindi (pasife alındı)!");
                return "redirect:/sahip";
        }

        // ============ MAĞAZA YÖNETİMİ ============
        @GetMapping("/magaza/{id}")
        public String magazaYonetim(@PathVariable Long id, Authentication auth, Model model) {
                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                Magaza magaza = magazaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

                // Sahiplik kontrolü
                if (!magaza.getSahip().getId().equals(kullanici.getId())) {
                        return "redirect:/sahip";
                }

                List<Urun> urunler = urunRepository.findByMagazaId(id);

                model.addAttribute("magaza", magaza);
                model.addAttribute("urunler", urunler);

                return "sahip/magaza-yonetim";
        }

        // ============ ÜRÜN EKLEME FORMU ============
        @GetMapping("/magaza/{id}/urun-ekle")
        public String urunEkleForm(@PathVariable Long id, Authentication auth, Model model) {
                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                Magaza magaza = magazaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

                // Sahiplik kontrolü
                if (!magaza.getSahip().getId().equals(kullanici.getId())) {
                        return "redirect:/sahip";
                }

                List<Kategori> kategoriler = kategoriRepository.findAll();
                List<AltKategori> altKategoriler = altKategoriRepository.findAll();
                List<Beden> bedenler = bedenRepository.findAll();

                model.addAttribute("magaza", magaza);
                model.addAttribute("kategoriler", kategoriler);
                model.addAttribute("altKategoriler", altKategoriler);
                model.addAttribute("bedenler", bedenler);

                return "sahip/urun-ekle";
        }

        // ============ ÜRÜN KAYDET ============
        @PostMapping("/magaza/{id}/urun-ekle")
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

                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                Magaza magaza = magazaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

                // Sahiplik kontrolü
                if (!magaza.getSahip().getId().equals(kullanici.getId())) {
                        return "redirect:/sahip";
                }

                AltKategori altKategori = altKategoriRepository.findById(altKategoriId)
                                .orElseThrow(() -> new RuntimeException("Alt kategori bulunamadı"));

                // Ürün oluştur
                Urun urun = new Urun();
                urun.setAd(ad);
                urun.setAciklama(aciklama);
                urun.setFiyat(fiyat);
                urun.setRenk(renk);
                urun.setResimUrl(resimUrl);
                urun.setAltKategori(altKategori);
                urun.setMagaza(magaza);
                urun.setSezon(Sezon.TUM_SEZON);
                urun.setAktif(true);
                urun = urunRepository.save(urun);

                // Stokları ekle
                for (int i = 0; i < bedenIds.size(); i++) {
                        if (stoklar.get(i) > 0) {
                                Beden beden = bedenRepository.findById(bedenIds.get(i))
                                                .orElseThrow(() -> new RuntimeException("Beden bulunamadı"));

                                UrunStok stok = new UrunStok();
                                stok.setUrun(urun);
                                stok.setBeden(beden);
                                stok.setAdet(stoklar.get(i));
                                urunStokRepository.save(stok);
                        }
                }

                redirectAttributes.addFlashAttribute("basari", "Ürün başarıyla eklendi!");
                return "redirect:/sahip/magaza/" + id;
        }

        // ============ ÜRÜN DÜZENLEME FORMU ============
        @GetMapping("/magaza/{magazaId}/urun-duzenle/{urunId}")
        public String urunDuzenleForm(@PathVariable Long magazaId, @PathVariable Long urunId, Authentication auth,
                        Model model) {
                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                Magaza magaza = magazaRepository.findById(magazaId)
                                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

                Urun urun = urunRepository.findById(urunId)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

                // Sahiplik kontrolü
                if (!magaza.getSahip().getId().equals(kullanici.getId())
                                || !urun.getMagaza().getId().equals(magaza.getId())) {
                        return "redirect:/sahip";
                }

                List<Kategori> kategoriler = kategoriRepository.findAll();
                List<AltKategori> altKategoriler = altKategoriRepository.findAll();
                List<Beden> bedenler = bedenRepository.findAll();
                List<UrunStok> mevcutStoklar = urunStokRepository.findByUrunId(urunId);

                model.addAttribute("magaza", magaza);
                model.addAttribute("urun", urun);
                model.addAttribute("kategoriler", kategoriler);
                model.addAttribute("altKategoriler", altKategoriler);
                model.addAttribute("bedenler", bedenler);
                model.addAttribute("mevcutStoklar", mevcutStoklar);

                return "sahip/urun-duzenle";
        }

        // ============ ÜRÜN DÜZENLE KAYDET ============
        @PostMapping("/magaza/{magazaId}/urun-duzenle/{urunId}")
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

                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                Magaza magaza = magazaRepository.findById(magazaId)
                                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

                Urun urun = urunRepository.findById(urunId)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

                // Sahiplik kontrolü
                if (!magaza.getSahip().getId().equals(kullanici.getId())
                                || !urun.getMagaza().getId().equals(magaza.getId())) {
                        return "redirect:/sahip";
                }

                AltKategori altKategori = altKategoriRepository.findById(altKategoriId)
                                .orElseThrow(() -> new RuntimeException("Alt kategori bulunamadı"));

                // Ürün Güncelle
                urun.setAd(ad);
                urun.setAciklama(aciklama);
                urun.setFiyat(fiyat);
                urun.setRenk(renk);
                urun.setResimUrl(resimUrl);
                urun.setAltKategori(altKategori);
                urun = urunRepository.save(urun);

                // Stokları Güncelle (Mevcut stokları silip yeniden ekle veya güncelle -
                // basitlik için yeniden ekliyoruz)
                // Not: Gerçek hayatta bu işlem transaction içinde olmalı ve mevcut siparişleri
                // etkilememeli.
                // Burada basitçe var olan stok kayıtlarını güncelleyeceğiz veya yenilerini
                // ekleyeceğiz.

                if (bedenIds != null && stoklar != null) {
                        List<UrunStok> eskiStoklar = urunStokRepository.findByUrunId(urunId);
                        urunStokRepository.deleteAll(eskiStoklar); // Öncekileri temizle

                        for (int i = 0; i < bedenIds.size(); i++) {
                                if (i < stoklar.size() && stoklar.get(i) != null && stoklar.get(i) > 0) {
                                        Beden beden = bedenRepository.findById(bedenIds.get(i))
                                                        .orElseThrow(() -> new RuntimeException("Beden bulunamadı"));

                                        UrunStok stok = new UrunStok();
                                        stok.setUrun(urun);
                                        stok.setBeden(beden);
                                        stok.setAdet(stoklar.get(i));
                                        urunStokRepository.save(stok);
                                }
                        }
                }

                redirectAttributes.addFlashAttribute("basari", "Ürün başarıyla güncellendi!");
                return "redirect:/sahip/magaza/" + magazaId;
        }

        // ============ ÜRÜN PASİFE AL ============
        @PostMapping("/urun/{id}/pasif")
        public String urunPasifYap(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                Urun urun = urunRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

                // Sahiplik kontrolü
                if (!urun.getMagaza().getSahip().getId().equals(kullanici.getId())) {
                        return "redirect:/sahip";
                }

                Long magazaId = urun.getMagaza().getId();

                urun.setAktif(false);
                urunRepository.save(urun);

                redirectAttributes.addFlashAttribute("basari", "Ürün pasife alındı!");
                return "redirect:/sahip/magaza/" + magazaId;
        }

        // ============ ÜRÜN AKTİFE AL ============
        @PostMapping("/urun/{id}/aktif")
        public String urunAktifYap(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                Urun urun = urunRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

                // Sahiplik kontrolü
                if (!urun.getMagaza().getSahip().getId().equals(kullanici.getId())) {
                        return "redirect:/sahip";
                }

                Long magazaId = urun.getMagaza().getId();

                urun.setAktif(true);
                urunRepository.save(urun);

                redirectAttributes.addFlashAttribute("basari", "Ürün aktife alındı!");
                return "redirect:/sahip/magaza/" + magazaId;
        }

        // ============ ÜRÜN KALICI SİL ============
        @PostMapping("/urun/{id}/sil")
        @Transactional
        public String urunKaliciSil(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                Urun urun = urunRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

                // Sahiplik kontrolü
                if (!urun.getMagaza().getSahip().getId().equals(kullanici.getId())) {
                        return "redirect:/sahip";
                }

                Long magazaId = urun.getMagaza().getId();

                // Stokları sil
                urunStokRepository.deleteByUrunId(id);

                // Ürünü kalıcı olarak sil
                urunRepository.delete(urun);

                redirectAttributes.addFlashAttribute("basari", "Ürün kalıcı olarak silindi!");
                return "redirect:/sahip/magaza/" + magazaId;
        }

        // ============ SİPARİŞ YÖNETİMİ ============
        @GetMapping("/magaza/{id}/siparisler")
        public String magazaSiparisler(@PathVariable Long id, Authentication auth, Model model) {
                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                Magaza magaza = magazaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

                // Sahiplik kontrolü
                if (!magaza.getSahip().getId().equals(kullanici.getId())) {
                        return "redirect:/sahip";
                }

                // Mağazanın siparişlerini bul
                List<SiparisFisi> siparisler = siparisFisiRepository.findByMagazaIdOrderBySiparisTarihiDesc(id);

                // Ciro hesapla
                BigDecimal toplamCiro = siparisler.stream()
                                .filter(s -> s.getDurum() == SiparisDurum.TESLIM_EDILDI)
                                .map(SiparisFisi::getToplamTutar)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal bekleyenCiro = siparisler.stream()
                                .filter(s -> s.getDurum() != SiparisDurum.TESLIM_EDILDI
                                                && s.getDurum() != SiparisDurum.IPTAL)
                                .map(SiparisFisi::getToplamTutar)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                long bekleyenSiparis = siparisler.stream()
                                .filter(s -> s.getDurum() == SiparisDurum.BEKLEMEDE)
                                .count();

                model.addAttribute("magaza", magaza);
                model.addAttribute("siparisler", siparisler);
                model.addAttribute("toplamCiro", toplamCiro);
                model.addAttribute("bekleyenCiro", bekleyenCiro);
                model.addAttribute("bekleyenSiparis", bekleyenSiparis);

                return "sahip/siparisler";
        }

        // ============ SİPARİŞ DURUMU GÜNCELLE ============
        @PostMapping("/siparis/{id}/guncelle")
        public String siparisDurumuGuncelle(@PathVariable Long id,
                        @RequestParam String durum,
                        Authentication auth,
                        RedirectAttributes redirectAttributes) {
                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                SiparisFisi siparis = siparisFisiRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

                // Sahiplik kontrolü
                if (!siparis.getMagaza().getSahip().getId().equals(kullanici.getId())) {
                        return "redirect:/sahip";
                }

                Long magazaId = siparis.getMagaza().getId();

                // Durumu güncelle
                try {
                        siparis.setDurum(SiparisDurum.valueOf(durum));
                        siparisFisiRepository.save(siparis);
                        redirectAttributes.addFlashAttribute("basari", "Sipariş durumu güncellendi!");
                } catch (IllegalArgumentException e) {
                        redirectAttributes.addFlashAttribute("hata", "Geçersiz durum!");
                }

                return "redirect:/sahip/magaza/" + magazaId + "/siparisler";
        }

        // ============ SİPARİŞ DÜZENLE SAYFASI ============
        @GetMapping("/siparis/{id}/duzenle")
        @Transactional(readOnly = true)
        public String siparisDuzenleSayfasi(@PathVariable Long id, Authentication auth, Model model,
                        RedirectAttributes redirectAttributes) {
                try {
                        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                        .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                        SiparisFisi siparis = siparisFisiRepository.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

                        // Null kontrolü
                        if (siparis.getMagaza() == null || siparis.getMagaza().getSahip() == null) {
                                redirectAttributes.addFlashAttribute("hata", "Sipariş bilgilerinde eksiklik var!");
                                return "redirect:/sahip";
                        }

                        // Sahiplik kontrolü
                        if (!siparis.getMagaza().getSahip().getId().equals(kullanici.getId())) {
                                redirectAttributes.addFlashAttribute("hata", "Bu siparişi görüntüleme yetkiniz yok!");
                                return "redirect:/sahip";
                        }

                        List<SiparisDetay> siparisDetaylari = siparisDetayRepository.findBySiparisFisiId(id);

                        model.addAttribute("siparis", siparis);
                        model.addAttribute("siparisDetaylari", siparisDetaylari);
                        model.addAttribute("magaza", siparis.getMagaza());

                        return "sahip/siparis-duzenle";
                } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("hata", "Sipariş yüklenirken hata: " + e.getMessage());
                        return "redirect:/sahip";
                }
        }

        // ============ SİPARİŞ DURUMU GÜNCELLE (Düzenleme sayfasından) ============
        @PostMapping("/siparis/{id}/durum-guncelle")
        public String siparisDurumDuzenle(@PathVariable Long id,
                        @RequestParam String durum,
                        Authentication auth,
                        RedirectAttributes redirectAttributes) {
                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                SiparisFisi siparis = siparisFisiRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

                // Sahiplik kontrolü
                if (!siparis.getMagaza().getSahip().getId().equals(kullanici.getId())) {
                        return "redirect:/sahip";
                }

                // Durumu güncelle
                try {
                        siparis.setDurum(SiparisDurum.valueOf(durum));
                        siparisFisiRepository.save(siparis);
                        redirectAttributes.addFlashAttribute("basari", "Sipariş durumu güncellendi!");
                } catch (IllegalArgumentException e) {
                        redirectAttributes.addFlashAttribute("hata", "Geçersiz durum!");
                }

                return "redirect:/sahip/siparis/" + id + "/duzenle";
        }

        // ============ CİRO RAPORLARI ============
        @GetMapping("/magaza/{id}/ciro")
        public String magazaCiro(@PathVariable Long id, Authentication auth, Model model) {
                Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

                Magaza magaza = magazaRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

                // Sahiplik kontrolü
                if (!magaza.getSahip().getId().equals(kullanici.getId())) {
                        return "redirect:/sahip";
                }

                List<SiparisFisi> tumSiparisler = siparisFisiRepository.findByMagazaIdOrderBySiparisTarihiDesc(id);

                // Teslim edilmiş siparişleri filtrele
                List<SiparisFisi> teslimEdilenler = tumSiparisler.stream()
                                .filter(s -> s.getDurum() == SiparisDurum.TESLIM_EDILDI)
                                .toList();

                BigDecimal toplamCiro = teslimEdilenler.stream()
                                .map(SiparisFisi::getToplamTutar)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                long toplamSiparis = teslimEdilenler.size();

                model.addAttribute("magaza", magaza);
                model.addAttribute("toplamCiro", toplamCiro);
                model.addAttribute("toplamSiparis", toplamSiparis);
                model.addAttribute("siparisler", teslimEdilenler);

                return "sahip/ciro";
        }
}
