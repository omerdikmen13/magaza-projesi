package com.magazaapp.controller;

import com.magazaapp.model.KullaniciRol;
import com.magazaapp.service.KullaniciService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class KullaniciController {

    private final KullaniciService kullaniciService;

    public KullaniciController(KullaniciService kullaniciService) {
        this.kullaniciService = kullaniciService;
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

        try {
            // Güvenlik: Sadece MUSTERI ve MAGAZA_SAHIBI rolü kabul et
            KullaniciRol kullaniciRol = "MAGAZA_SAHIBI".equals(rol)
                    ? KullaniciRol.MAGAZA_SAHIBI
                    : KullaniciRol.MUSTERI;

            // Service katmanında tüm validasyon ve kayıt yapılacak
            kullaniciService.registerUser(kullaniciAdi, email, sifre, ad, soyad, kullaniciRol);

            return "redirect:/giris?kayit=basarili";
        } catch (RuntimeException e) {
            model.addAttribute("hata", e.getMessage());
            return "uye-ol";
        }
    }

    // ============ ANA SAYFA ============
    @GetMapping("/")
    public String anaSayfa() {
        return "index";
    }
}
