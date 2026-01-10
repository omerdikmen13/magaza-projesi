package com.magazaapp.controller;

import com.magazaapp.model.*;
import com.magazaapp.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class UrunController {

    private final UrunRepository urunRepository;
    private final UrunStokRepository urunStokRepository;
    private final SepetRepository sepetRepository;
    private final KullaniciRepository kullaniciRepository;
    private final BedenRepository bedenRepository;

    public UrunController(UrunRepository urunRepository, UrunStokRepository urunStokRepository,
            SepetRepository sepetRepository, KullaniciRepository kullaniciRepository,
            BedenRepository bedenRepository) {
        this.urunRepository = urunRepository;
        this.urunStokRepository = urunStokRepository;
        this.sepetRepository = sepetRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.bedenRepository = bedenRepository;
    }

    // ============ ARAMA (WEB) ============
    @GetMapping("/ara")
    public String ara(@RequestParam(required = false) String q, Model model) {
        if (q == null || q.trim().length() < 2) {
            model.addAttribute("hata", "Arama terimi en az 2 karakter olmalıdır");
            model.addAttribute("sonuclar", List.of());
            model.addAttribute("aramaKelimesi", q != null ? q : "");
            return "ara";
        }

        String aramaKelimesi = q.trim().toLowerCase();
        List<Urun> tumUrunler = urunRepository.findByAktifTrue();

        // Ürün adı, açıklama, mağaza adı veya kategori ile eşleştir
        List<Urun> sonuclar = tumUrunler.stream()
                .filter(urun -> {
                    String ad = urun.getAd() != null ? urun.getAd().toLowerCase() : "";
                    String aciklama = urun.getAciklama() != null ? urun.getAciklama().toLowerCase() : "";
                    String magazaAd = urun.getMagaza() != null && urun.getMagaza().getAd() != null
                            ? urun.getMagaza().getAd().toLowerCase()
                            : "";
                    String kategori = urun.getAltKategori() != null && urun.getAltKategori().getKategori() != null
                            ? urun.getAltKategori().getKategori().getAd().toLowerCase()
                            : "";
                    String altKategori = urun.getAltKategori() != null
                            ? urun.getAltKategori().getAd().toLowerCase()
                            : "";
                    String renk = urun.getRenk() != null ? urun.getRenk().toLowerCase() : "";

                    return ad.contains(aramaKelimesi) ||
                            aciklama.contains(aramaKelimesi) ||
                            magazaAd.contains(aramaKelimesi) ||
                            kategori.contains(aramaKelimesi) ||
                            altKategori.contains(aramaKelimesi) ||
                            renk.contains(aramaKelimesi);
                })
                .toList();

        model.addAttribute("sonuclar", sonuclar);
        model.addAttribute("aramaKelimesi", q);
        model.addAttribute("toplamSonuc", sonuclar.size());

        return "ara";
    }

    // ============ ÜRÜN DETAY ============
    @GetMapping("/urun/{id}")
    public String urunDetay(@PathVariable Long id, Authentication auth, Model model) {
        Urun urun = urunRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

        List<UrunStok> stoklar = urunStokRepository.findByUrunId(id);

        // Giriş yapmış kullanıcıyı model'e ekle
        if (auth != null) {
            Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                    .orElse(null);
            model.addAttribute("kullanici", kullanici);
        }

        model.addAttribute("urun", urun);
        model.addAttribute("stoklar", stoklar);
        model.addAttribute("isFavorite", false); // Geçici olarak false

        return "urun-detay";
    }

    // ============ SEPETE EKLE ============
    @PostMapping("/urun/{id}/sepete-ekle")
    @Transactional
    public String sepeteEkle(@PathVariable Long id,
            @RequestParam Long bedenId,
            @RequestParam(defaultValue = "1") Integer adet,
            @RequestParam(required = false) Boolean farkliMagazaOnay,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            Urun urun = urunRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            Beden beden = bedenRepository.findById(bedenId)
                    .orElseThrow(() -> new RuntimeException("Beden bulunamadı"));

            // Farklı mağaza kontrolü
            List<Sepet> mevcutSepet = sepetRepository.findByKullaniciId(kullanici.getId());
            if (!mevcutSepet.isEmpty()) {
                Long sepettekiMagazaId = mevcutSepet.get(0).getUrun().getMagaza().getId();
                if (!sepettekiMagazaId.equals(urun.getMagaza().getId())) {
                    // Onay verilmemişse uyarı göster
                    if (farkliMagazaOnay == null || !farkliMagazaOnay) {
                        redirectAttributes.addFlashAttribute("farkliMagazaUyari", true);
                        redirectAttributes.addFlashAttribute("mevcutMagaza",
                                mevcutSepet.get(0).getUrun().getMagaza().getAd());
                        redirectAttributes.addFlashAttribute("yeniMagaza", urun.getMagaza().getAd());
                        redirectAttributes.addFlashAttribute("bekleyenBedenId", bedenId);
                        redirectAttributes.addFlashAttribute("bekleyenAdet", adet);
                        return "redirect:/urun/" + id;
                    } else {
                        // Onay verilmiş, sepeti boşalt
                        sepetRepository.deleteByKullaniciId(kullanici.getId());
                    }
                }
            }

            // Stok kontrolü
            UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(id, bedenId)
                    .orElseThrow(() -> new RuntimeException("Stok bulunamadı"));

            // Sepette zaten var mı?
            Sepet sepetItem = sepetRepository.findByKullaniciIdAndUrunIdAndBedenId(
                    kullanici.getId(), id, bedenId)
                    .orElse(new Sepet());

            // Toplam miktar kontrolü (mevcut sepetteki + yeni eklenen)
            int mevcutSepetMiktari = (sepetItem.getId() != null) ? sepetItem.getAdet() : 0;
            int toplamMiktar = mevcutSepetMiktari + adet;

            if (stok.getAdet() < toplamMiktar) {
                String mesaj = mevcutSepetMiktari > 0
                        ? "Yeterli stok yok! Sepetinizde zaten " + mevcutSepetMiktari + " adet var. Maksimum "
                                + stok.getAdet() + " adet ekleyebilirsiniz."
                        : "Yeterli stok yok! Maksimum " + stok.getAdet() + " adet ekleyebilirsiniz.";
                redirectAttributes.addFlashAttribute("hata", mesaj);
                return "redirect:/urun/" + id;
            }

            if (sepetItem.getId() == null) {
                sepetItem.setKullanici(kullanici);
                sepetItem.setUrun(urun);
                sepetItem.setBeden(beden);
                sepetItem.setAdet(adet);
            } else {
                sepetItem.setAdet(sepetItem.getAdet() + adet);
            }

            sepetRepository.save(sepetItem);

            redirectAttributes.addFlashAttribute("basari", "Ürün sepete eklendi!");
            return "redirect:/sepet";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Hata: " + e.getMessage());
            return "redirect:/urun/" + id;
        }
    }
}
