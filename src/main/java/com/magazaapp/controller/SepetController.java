package com.magazaapp.controller;

import com.magazaapp.model.*;
import com.magazaapp.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class SepetController {

    private final SepetService sepetService;
    private final KullaniciService kullaniciService;
    private final SiparisService siparisService;

    public SepetController(SepetService sepetService,
            KullaniciService kullaniciService,
            SiparisService siparisService) {
        this.sepetService = sepetService;
        this.kullaniciService = kullaniciService;
        this.siparisService = siparisService;
    }

    // ============ SEPET GÖSTER ============
    @GetMapping("/sepet")
    public String sepet(Authentication auth, Model model) {
        Kullanici kullanici = kullaniciService.getByUsername(auth.getName());

        List<Sepet> sepetListesi = sepetService.getSepetByKullanici(kullanici.getId());
        BigDecimal toplam = sepetService.getSepetToplam(kullanici.getId());

        model.addAttribute("sepetListesi", sepetListesi);
        model.addAttribute("toplam", toplam);
        model.addAttribute("kullanici", kullanici);

        return "sepet";
    }

    // ============ SEPETTEN SİL ============
    @PostMapping("/sepet/sil/{id}")
    public String sepettenSil(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        sepetService.sepetItemSil(id);
        redirectAttributes.addFlashAttribute("basari", "Ürün sepetten silindi!");
        return "redirect:/sepet";
    }

    // ============ SEPET GÜNCELLE ============
    @PostMapping("/sepet/guncelle/{id}")
    public String sepetGuncelle(@PathVariable Long id, @RequestParam int adet,
            RedirectAttributes redirectAttributes) {
        try {
            if (adet <= 0) {
                sepetService.sepetItemSil(id);
                redirectAttributes.addFlashAttribute("basari", "Ürün sepetten silindi!");
            } else {
                sepetService.sepetItemGuncelle(id, adet);
                redirectAttributes.addFlashAttribute("basari", "Sepet güncellendi!");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("hata", e.getMessage());
        }
        return "redirect:/sepet";
    }

    // ============ SEPETİ BOŞALT ============
    @PostMapping("/sepet/bosalt")
    public String sepetiBosalt(Authentication auth, RedirectAttributes redirectAttributes) {
        Kullanici kullanici = kullaniciService.getByUsername(auth.getName());
        sepetService.sepetiTemizle(kullanici.getId());
        redirectAttributes.addFlashAttribute("basari", "Sepet boşaltıldı!");
        return "redirect:/sepet";
    }

    // ============ SİPARİŞ VER ============
    @PostMapping("/sepet/siparis-ver")
    public String siparisVer(Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Kullanici kullanici = kullaniciService.getByUsername(auth.getName());

            // Service katmanında tüm sipariş logic'i (stok kontrolü, sipariş oluşturma,
            // sepet temizleme)
            SiparisFisi siparis = siparisService.sepettenSiparisOlustur(kullanici.getId());

            redirectAttributes.addFlashAttribute("basari",
                    "Siparişiniz başarıyla oluşturuldu! Sipariş No: #" + siparis.getId());
            return "redirect:/siparislerim";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("hata", e.getMessage());
            return "redirect:/sepet";
        }
    }
}
