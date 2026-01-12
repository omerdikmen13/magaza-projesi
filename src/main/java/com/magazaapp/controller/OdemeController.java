package com.magazaapp.controller;

import com.magazaapp.model.*;
import com.magazaapp.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * MOCK Ödeme Controller - Web tarafı
 * Gerçek ödeme sistemi yerine simüle edilmiş ödeme
 */
@Controller
@RequestMapping("/odeme")
public class OdemeController {

    private final MockOdemeService mockOdemeService;
    private final KullaniciService kullaniciService;
    private final SepetService sepetService;
    private final SiparisService siparisService;

    public OdemeController(MockOdemeService mockOdemeService, KullaniciService kullaniciService,
            SepetService sepetService, SiparisService siparisService) {
        this.mockOdemeService = mockOdemeService;
        this.kullaniciService = kullaniciService;
        this.sepetService = sepetService;
        this.siparisService = siparisService;
    }

    /**
     * Ödeme sayfasını göster - Kart formu
     */
    @GetMapping
    public String odemeSayfasi(Authentication auth, Model model) {
        Kullanici kullanici = kullaniciService.getByUsername(auth.getName());

        // Sepet kontrolü
        List<Sepet> sepet = sepetService.getKullaniciSepeti(kullanici.getId());
        if (sepet.isEmpty()) {
            return "redirect:/sepet";
        }

        try {
            // Mock ödeme başlat
            MockOdemeService.OdemeBaslatSonuc sonuc = mockOdemeService.odemeBaslat(kullanici);

            model.addAttribute("token", sonuc.getToken());
            model.addAttribute("tutar", sonuc.getTutar());
            model.addAttribute("sepet", sepet);

            return "odeme";
        } catch (Exception e) {
            model.addAttribute("hata", e.getMessage());
            return "odeme-hata";
        }
    }

    /**
     * Ödeme formunu işle - Kart bilgileri
     */
    @PostMapping("/tamamla")
    public String odemeTamamla(@RequestParam String token,
            @RequestParam String kartNo,
            @RequestParam String sonKullanma,
            @RequestParam String cvv,
            @RequestParam String kartSahibi,
            Authentication auth,
            RedirectAttributes redirectAttributes) {
        try {
            MockOdemeService.OdemeSonuc sonuc = mockOdemeService.odemeTamamla(
                    token, kartNo, sonKullanma, cvv, kartSahibi);

            if (sonuc.isBasarili()) {
                // Sipariş oluştur
                Kullanici kullanici = kullaniciService.getByUsername(auth.getName());
                SiparisFisi siparis = siparisService.sepettenSiparisOlustur(kullanici.getId());

                // Ödeme ile siparişi ilişkilendir
                Odeme odeme = sonuc.getOdeme();
                odeme.setSiparisFisi(siparis);

                redirectAttributes.addFlashAttribute("basari",
                        "Ödeme başarılı! Sipariş No: #" + siparis.getId());
                return "redirect:/siparislerim";
            } else {
                redirectAttributes.addFlashAttribute("hata", sonuc.getMesaj());
                return "redirect:/odeme/hata";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", e.getMessage());
            return "redirect:/odeme/hata";
        }
    }

    /**
     * Ödeme hata sayfası
     */
    @GetMapping("/hata")
    public String odemeHata(Model model) {
        return "odeme-hata";
    }

    /**
     * Ödeme başarılı sayfası
     */
    @GetMapping("/basarili")
    public String odemeBasarili(@RequestParam(required = false) Long siparisId, Model model) {
        if (siparisId != null) {
            model.addAttribute("siparisId", siparisId);
        }
        return "odeme-basarili";
    }
}
