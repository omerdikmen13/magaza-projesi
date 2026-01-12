package com.magazaapp.controller;

import com.magazaapp.model.Kullanici;
import com.magazaapp.service.KullaniciService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hesabim")
public class HesabimController {

    private final KullaniciService kullaniciService;

    public HesabimController(KullaniciService kullaniciService) {
        this.kullaniciService = kullaniciService;
    }

    // Hesabım sayfası
    @GetMapping
    public String hesabim(Authentication authentication, Model model) {
        Kullanici kullanici = kullaniciService.getByUsername(authentication.getName());
        model.addAttribute("kullanici", kullanici);
        return "hesabim";
    }

    // Profil güncelleme
    @PostMapping("/guncelle")
    public String profilGuncelle(
            Authentication authentication,
            @RequestParam String ad,
            @RequestParam String soyad,
            @RequestParam String email,
            @RequestParam(required = false) String telefon,
            @RequestParam(required = false) String adres,
            RedirectAttributes redirectAttributes) {

        try {
            Kullanici kullanici = kullaniciService.getByUsername(authentication.getName());

            // Service katmanında email uniqueness kontrolü yapılacak
            kullaniciService.updateProfile(kullanici.getId(), ad, soyad, email, telefon, adres);

            redirectAttributes.addFlashAttribute("basari", "Profil bilgileriniz güncellendi!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("hata", e.getMessage());
        }

        return "redirect:/hesabim";
    }

    // Şifre değiştirme
    @PostMapping("/sifre-degistir")
    public String sifreDegistir(
            Authentication authentication,
            @RequestParam String mevcutSifre,
            @RequestParam String yeniSifre,
            @RequestParam String yeniSifreTekrar,
            RedirectAttributes redirectAttributes) {

        try {
            Kullanici kullanici = kullaniciService.getByUsername(authentication.getName());

            // Service katmanında tüm validasyon yapılacak
            kullaniciService.changePassword(kullanici.getId(), mevcutSifre, yeniSifre, yeniSifreTekrar);

            redirectAttributes.addFlashAttribute("sifreBasari", "Şifreniz başarıyla değiştirildi!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("sifreHata", e.getMessage());
        }

        return "redirect:/hesabim";
    }
}
