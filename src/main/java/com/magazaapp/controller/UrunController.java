package com.magazaapp.controller;

import com.magazaapp.model.*;
import com.magazaapp.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class UrunController {

    private final UrunService urunService;
    private final SepetService sepetService;
    private final KullaniciService kullaniciService;
    private final KategoriService kategoriService;
    private final FavoriService favoriService;

    public UrunController(UrunService urunService, SepetService sepetService,
            KullaniciService kullaniciService,
            KategoriService kategoriService,
            FavoriService favoriService) {
        this.urunService = urunService;
        this.sepetService = sepetService;
        this.kullaniciService = kullaniciService;
        this.kategoriService = kategoriService;
        this.favoriService = favoriService;
    }

    // ============ ÜRÜN ARAMA ============
    @GetMapping("/ara")
    public String ara(@RequestParam(required = false) String q, Model model) {
        if (q != null && !q.trim().isEmpty()) {
            List<Urun> sonuclar = urunService.araUrun(q);
            model.addAttribute("sonuclar", sonuclar);
            model.addAttribute("toplamSonuc", sonuclar.size());
            model.addAttribute("aramaKelimesi", q);
        }
        return "ara";
    }

    // ============ ÜRÜN DETAY ============
    @GetMapping("/urun/{id}")
    public String urunDetay(@PathVariable Long id, Authentication auth, Model model) {
        Urun urun = urunService.getUrunById(id);
        List<UrunStok> stoklar = urunService.getUrunStoklari(id);

        // Favori kontrolü - kullanici GlobalControllerAdvice tarafından ekleniyor
        boolean isFavorite = false;
        if (auth != null) {
            try {
                Kullanici kullanici = kullaniciService.getByUsername(auth.getName());
                if (kullanici != null && kullanici.getRol() != null && kullanici.getRol() == KullaniciRol.MUSTERI) {
                    isFavorite = favoriService.favorideMi(kullanici.getId(), id);
                }
            } catch (Exception e) {
                // Favori kontrolü başarısız olursa sessizce devam et
                System.err.println("Favori kontrolu basarisiz: " + e.getMessage());
            }
        }

        model.addAttribute("urun", urun);
        model.addAttribute("stoklar", stoklar);
        model.addAttribute("isFavorite", isFavorite);

        return "urun-detay";
    }

    // ============ SEPETE EKLE ============
    @PostMapping("/sepete-ekle")
    public String sepeteEkle(@RequestParam Long urunId,
            @RequestParam Long bedenId,
            @RequestParam(defaultValue = "1") int adet,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        if (auth == null) {
            return "redirect:/giris";
        }

        try {
            Kullanici kullanici = kullaniciService.getByUsername(auth.getName());

            // Service katmanında stok kontrolü ve sepete ekleme yapılacak
            var result = sepetService.sepeteEkle(kullanici, urunId, bedenId, adet, null);

            if (result.isSuccess()) {
                redirectAttributes.addFlashAttribute("basari", result.getBasariMesaji());
            } else if (result.isFarkliMagazaUyari()) {
                redirectAttributes.addFlashAttribute("farkliMagazaUyari", true);
                redirectAttributes.addFlashAttribute("mevcutMagaza", result.getMevcutMagazaAd());
                redirectAttributes.addFlashAttribute("yeniMagaza", result.getYeniMagazaAd());
            } else {
                redirectAttributes.addFlashAttribute("hata", result.getHataMesaji());
            }

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("hata", e.getMessage());
        }

        return "redirect:/urun/" + urunId;
    }

    // ============ FİLTRELİ ÜRÜN LİSTESİ ============
    @GetMapping("/urunler")
    public String urunler(@RequestParam(required = false) Long magazaId,
            @RequestParam(required = false) Long kategoriId,
            @RequestParam(required = false) Long altKategoriId,
            @RequestParam(required = false) String siralama,
            Model model) {

        List<Urun> urunler = urunService.getUrunlerByFiltre(magazaId, kategoriId, altKategoriId);

        // Sıralama
        if (siralama != null) {
            urunler = urunService.siralaUrunler(urunler, siralama);
        }

        List<Kategori> kategoriler = kategoriService.getTumKategoriler();

        model.addAttribute("urunler", urunler);
        model.addAttribute("kategoriler", kategoriler);
        model.addAttribute("siralama", siralama);

        return "urunler";
    }
}
