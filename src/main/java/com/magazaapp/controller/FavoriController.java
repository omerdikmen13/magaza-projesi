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
public class FavoriController {

    private final FavoriRepository favoriRepository;
    private final KullaniciRepository kullaniciRepository;
    private final UrunRepository urunRepository;

    public FavoriController(FavoriRepository favoriRepository,
            KullaniciRepository kullaniciRepository,
            UrunRepository urunRepository) {
        this.favoriRepository = favoriRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.urunRepository = urunRepository;
    }

    // ============ FAVORİLERİM SAYFASI ============
    @GetMapping("/favorilerim")
    public String favorilerim(Authentication auth, Model model) {
        if (auth == null) {
            return "redirect:/giris";
        }

        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        List<Favori> favoriler = favoriRepository.findByKullaniciOrderByEklenmeTarihiDesc(kullanici);

        model.addAttribute("favoriler", favoriler);
        model.addAttribute("kullanici", kullanici);

        return "favorilerim";
    }

    // ============ FAVORİYE EKLE/KALDIR (TOGGLE) ============
    @PostMapping("/favori/toggle/{urunId}")
    @Transactional
    public String favoriToggle(@PathVariable Long urunId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/giris";
        }

        try {
            Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            Urun urun = urunRepository.findById(urunId)
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            var mevcutFavori = favoriRepository.findByKullaniciAndUrun(kullanici, urun);

            if (mevcutFavori.isPresent()) {
                // Favoriden kaldır
                favoriRepository.delete(mevcutFavori.get());
                redirectAttributes.addFlashAttribute("basari", "Ürün favorilerden kaldırıldı");
            } else {
                // Favoriye ekle
                Favori favori = new Favori(kullanici, urun);
                favoriRepository.save(favori);
                redirectAttributes.addFlashAttribute("basari", "Ürün favorilere eklendi");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Hata: " + e.getMessage());
        }

        return "redirect:/urun/" + urunId;
    }

    // ============ FAVORİDEN KALDIR ============
    @PostMapping("/favori/kaldir/{urunId}")
    @Transactional
    public String favoriKaldir(@PathVariable Long urunId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/giris";
        }

        try {
            Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            Urun urun = urunRepository.findById(urunId)
                    .orElseThrow(() -> new RuntimeException("Ürün bulunamadı"));

            favoriRepository.deleteByKullaniciAndUrun(kullanici, urun);
            redirectAttributes.addFlashAttribute("basari", "Ürün favorilerden kaldırıldı");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Hata: " + e.getMessage());
        }

        return "redirect:/favorilerim";
    }
}
