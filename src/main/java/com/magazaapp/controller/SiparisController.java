package com.magazaapp.controller;

import com.magazaapp.model.Kullanici;
import com.magazaapp.model.SiparisFisi;
import com.magazaapp.model.SiparisDetay;
import com.magazaapp.service.SiparisService;
import com.magazaapp.service.SiparisDetayService;
import com.magazaapp.service.KullaniciService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SiparisController {

    private final SiparisService siparisService;
    private final SiparisDetayService siparisDetayService;
    private final KullaniciService kullaniciService;

    public SiparisController(SiparisService siparisService,
            SiparisDetayService siparisDetayService,
            KullaniciService kullaniciService) {
        this.siparisService = siparisService;
        this.siparisDetayService = siparisDetayService;
        this.kullaniciService = kullaniciService;
    }

    // ============ SİPARİŞLERİM ============
    @GetMapping("/siparislerim")
    public String siparislerim(Authentication auth, Model model) {
        Kullanici kullanici = kullaniciService.getByUsername(auth.getName());
        List<SiparisFisi> siparisler = siparisService.getKullaniciSiparisleri(kullanici.getId());

        model.addAttribute("siparisler", siparisler);

        return "siparislerim";
    }

    // ============ SİPARİŞ DETAY ============
    @GetMapping("/siparis/{id}")
    public String siparisDetay(@PathVariable Long id, Authentication auth, Model model) {
        Kullanici kullanici = kullaniciService.getByUsername(auth.getName());
        SiparisFisi siparis = siparisService.getSiparisById(id);

        // Güvenlik kontrolü: Sadece kendi siparişlerini görebilir
        if (!siparis.getKullanici().getId().equals(kullanici.getId())) {
            throw new RuntimeException("Bu siparişi görme yetkiniz yok!");
        }

        List<SiparisDetay> detaylar = siparisDetayService.getDetaylarBySiparisFisi(id);

        model.addAttribute("siparis", siparis);
        model.addAttribute("detaylar", detaylar);

        return "siparis-detay";
    }
}
