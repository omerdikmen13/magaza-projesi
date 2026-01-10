package com.magazaapp.controller;

import com.magazaapp.model.Kullanici;
import com.magazaapp.repository.KullaniciRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/hesabim")
public class HesabimController {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;

    public HesabimController(KullaniciRepository kullaniciRepository, PasswordEncoder passwordEncoder) {
        this.kullaniciRepository = kullaniciRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Hesabım sayfası
    @GetMapping
    public String hesabim(Authentication authentication, Model model) {
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

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

        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Email değişmişse ve başkası kullanıyorsa kontrol et
        if (!kullanici.getEmail().equals(email) && kullaniciRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("hata", "Bu email adresi zaten kullanımda!");
            return "redirect:/hesabim";
        }

        kullanici.setAd(ad);
        kullanici.setSoyad(soyad);
        kullanici.setEmail(email);
        kullanici.setTelefon(telefon);
        kullanici.setAdres(adres);

        kullaniciRepository.save(kullanici);

        redirectAttributes.addFlashAttribute("basari", "Profil bilgileriniz güncellendi!");
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

        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Mevcut şifre kontrolü
        if (!passwordEncoder.matches(mevcutSifre, kullanici.getSifre())) {
            redirectAttributes.addFlashAttribute("sifreHata", "Mevcut şifreniz hatalı!");
            return "redirect:/hesabim";
        }

        // Yeni şifre kontrolü
        if (!yeniSifre.equals(yeniSifreTekrar)) {
            redirectAttributes.addFlashAttribute("sifreHata", "Yeni şifreler eşleşmiyor!");
            return "redirect:/hesabim";
        }

        if (yeniSifre.length() < 6) {
            redirectAttributes.addFlashAttribute("sifreHata", "Şifre en az 6 karakter olmalı!");
            return "redirect:/hesabim";
        }

        kullanici.setSifre(passwordEncoder.encode(yeniSifre));
        kullaniciRepository.save(kullanici);

        redirectAttributes.addFlashAttribute("sifreBasari", "Şifreniz başarıyla değiştirildi!");
        return "redirect:/hesabim";
    }
}
