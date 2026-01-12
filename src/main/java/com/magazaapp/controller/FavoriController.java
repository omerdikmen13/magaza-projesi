package com.magazaapp.controller;

import com.magazaapp.model.*;
import com.magazaapp.service.FavoriService;
import com.magazaapp.service.KullaniciService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class FavoriController {

    private final FavoriService favoriService;
    private final KullaniciService kullaniciService;

    public FavoriController(FavoriService favoriService,
            KullaniciService kullaniciService) {
        this.favoriService = favoriService;
        this.kullaniciService = kullaniciService;
    }

    // ============ FAVORİLERİM SAYFASI ============
    @GetMapping("/favorilerim")
    public String favorilerim(Authentication auth, Model model) {
        if (auth == null) {
            return "redirect:/giris";
        }

        Kullanici kullanici = kullaniciService.getByUsername(auth.getName());
        List<Favori> favoriler = favoriService.getKullaniciFavorileriList(kullanici.getId());

        model.addAttribute("favoriler", favoriler);
        model.addAttribute("kullanici", kullanici);

        return "favorilerim";
    }

    // ============ FAVORİYE EKLE/KALDIR (TOGGLE) ============
    @PostMapping("/favori/toggle/{urunId}")
    public String favoriToggle(@PathVariable Long urunId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/giris";
        }

        try {
            Kullanici kullanici = kullaniciService.getByUsername(auth.getName());

            // Toggle favori - service katmanında yapılacak
            boolean eklendi = favoriService.toggleFavori(kullanici.getId(), urunId);

            if (eklendi) {
                redirectAttributes.addFlashAttribute("basari", "Ürün favorilere eklendi");
            } else {
                redirectAttributes.addFlashAttribute("basari", "Ürün favorilerden kaldırıldı");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Hata: " + e.getMessage());
        }

        return "redirect:/urun/" + urunId;
    }

    // ============ FAVORİDEN KALDIR ============
    @PostMapping("/favori/kaldir/{urunId}")
    public String favoriKaldir(@PathVariable Long urunId,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        if (auth == null) {
            return "redirect:/giris";
        }

        try {
            Kullanici kullanici = kullaniciService.getByUsername(auth.getName());

            favoriService.removeFavori(kullanici.getId(), urunId);
            redirectAttributes.addFlashAttribute("basari", "Ürün favorilerden kaldırıldı");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Hata: " + e.getMessage());
        }

        return "redirect:/favorilerim";
    }
}
