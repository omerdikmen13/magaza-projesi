package com.magazaapp.controller;

import com.magazaapp.model.Kullanici;
import com.magazaapp.model.KullaniciRol;
import com.magazaapp.repository.KullaniciRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class KullaniciController {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;

    public KullaniciController(KullaniciRepository kullaniciRepository, PasswordEncoder passwordEncoder) {
        this.kullaniciRepository = kullaniciRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ============ GİRİŞ SAYFASI ============
    @GetMapping("/giris")
    public String girisForm() {
        return "giris";
    }

    // ============ ÜYE OL SAYFASI ============
    @GetMapping("/uye-ol")
    public String uyeOlForm() {
        return "uye-ol";
    }

    @PostMapping("/uye-ol")
    public String uyeOlKaydet(
            @RequestParam("kullaniciAdi") String kullaniciAdi,
            @RequestParam("email") String email,
            @RequestParam("sifre") String sifre,
            @RequestParam("ad") String ad,
            @RequestParam("soyad") String soyad,
            @RequestParam(value = "rol", defaultValue = "MUSTERI") String rol,
            Model model) {

        // Kullanıcı adı kontrolü
        Optional<Kullanici> varOlan = kullaniciRepository.findByKullaniciAdi(kullaniciAdi);
        if (varOlan.isPresent()) {
            model.addAttribute("hata", "Bu kullanıcı adı zaten kayıtlı!");
            return "uye-ol";
        }

        // Email kontrolü
        if (kullaniciRepository.existsByEmail(email)) {
            model.addAttribute("hata", "Bu email zaten kullanılıyor!");
            return "uye-ol";
        }

        // Güvenlik: Sadece MUSTERI ve MAGAZA_SAHIBI rolü kabul et
        KullaniciRol kullaniciRol;
        if ("MAGAZA_SAHIBI".equals(rol)) {
            kullaniciRol = KullaniciRol.MAGAZA_SAHIBI;
        } else {
            kullaniciRol = KullaniciRol.MUSTERI;
        }

        // Yeni kullanıcı oluştur
        Kullanici kullanici = new Kullanici();
        kullanici.setKullaniciAdi(kullaniciAdi);
        kullanici.setEmail(email);
        kullanici.setSifre(passwordEncoder.encode(sifre));
        kullanici.setAd(ad);
        kullanici.setSoyad(soyad);
        kullanici.setRol(kullaniciRol);

        kullaniciRepository.save(kullanici);

        return "redirect:/giris?kayit=basarili";
    }

    // ============ ANA SAYFA ============
    @GetMapping("/")
    public String anaSayfa() {
        return "index";
    }
}
