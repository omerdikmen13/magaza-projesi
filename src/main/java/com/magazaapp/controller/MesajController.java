package com.magazaapp.controller;

import com.magazaapp.model.*;
import com.magazaapp.service.MesajService;
import com.magazaapp.service.KullaniciService;
import com.magazaapp.service.MagazaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class MesajController {

    private final MesajService mesajService;
    private final KullaniciService kullaniciService;
    private final MagazaService magazaService;

    public MesajController(MesajService mesajService, KullaniciService kullaniciService,
            MagazaService magazaService) {
        this.mesajService = mesajService;
        this.kullaniciService = kullaniciService;
        this.magazaService = magazaService;
    }

    // ============ MÜŞTERİ: MESAJLARIM ============
    @GetMapping("/mesajlarim")
    public String mesajlarim(Authentication auth, Model model) {
        Kullanici kullanici = kullaniciService.getByUsername(auth.getName());

        // Son mesajları mağaza bazında grupla
        Map<Long, Mesaj> magazaSonMesaj = mesajService.getMusteriMagazaSonMesajlari(kullanici.getId());

        model.addAttribute("kullanici", kullanici);
        model.addAttribute("sohbetler", magazaSonMesaj.values());

        return "mesajlarim";
    }

    // ============ MÜŞTERİ: SOHBET SAYFASI ============
    @GetMapping("/sohbet/{magazaId}")
    public String sohbet(@PathVariable Long magazaId, Authentication auth, Model model) {
        Kullanici kullanici = kullaniciService.getByUsername(auth.getName());
        Magaza magaza = magazaService.getMagazaById(magazaId);

        // Mesajları getir ve okundu işaretle
        List<Mesaj> mesajlar = mesajService.getMagazaMusteriSohbet(magazaId, kullanici.getId(), true);

        model.addAttribute("kullanici", kullanici);
        model.addAttribute("magaza", magaza);
        model.addAttribute("mesajlar", mesajlar);

        return "sohbet";
    }

    // ============ MESAJ GÖNDER ============
    @PostMapping("/mesaj/gonder")
    public String mesajGonder(@RequestParam Long magazaId,
            @RequestParam String icerik,
            @RequestParam(required = false) Long musteriId,
            Authentication auth) {

        Kullanici kullanici = kullaniciService.getByUsername(auth.getName());
        Magaza magaza = magazaService.getMagazaById(magazaId);

        // Mağaza sahibi mi müşteri mi kontrol et ve mesajı gönder
        boolean gonderenMusteri = !magaza.getSahip().getId().equals(kullanici.getId());

        if (gonderenMusteri) {
            // Müşteri gönderiyor
            mesajService.musteridenMesajGonder(kullanici.getId(), magazaId, icerik);
            return "redirect:/sohbet/" + magazaId;
        } else {
            // Mağaza sahibi gönderiyor
            if (musteriId != null) {
                mesajService.magazadanMesajGonder(kullanici.getId(), magazaId, musteriId, icerik);
                return "redirect:/sahip/mesajlar/" + magazaId + "?musteriId=" + musteriId;
            }
            return "redirect:/sahip/mesajlar/" + magazaId;
        }
    }

    // ============ MAĞAZA SAHİBİ: MESAJLAR ============
    @GetMapping("/sahip/mesajlar")
    public String sahipMesajlar(Authentication auth, Model model) {
        Kullanici kullanici = kullaniciService.getByUsername(auth.getName());

        // Sahibin mağazaları ve mesaj istatistikleri
        Map<Magaza, Long> magazaOkunmamisSayisi = mesajService.getSahipMagazaOkunmamisSayilari(kullanici.getId());

        model.addAttribute("kullanici", kullanici);
        model.addAttribute("magazalar", magazaOkunmamisSayisi.keySet());
        model.addAttribute("okunmamisSayisi", magazaOkunmamisSayisi);

        return "sahip/mesajlar";
    }

    // ============ MAĞAZA SAHİBİ: BELİRLİ MAĞAZA MESAJLARI ============
    @GetMapping("/sahip/mesajlar/{magazaId}")
    public String sahipMagazaMesajlari(@PathVariable Long magazaId,
            @RequestParam(required = false) Long musteriId,
            Authentication auth, Model model) {

        Kullanici kullanici = kullaniciService.getByUsername(auth.getName());
        Magaza magaza = magazaService.getMagazaById(magazaId);

        // Yetki kontrolü
        if (!magaza.getSahip().getId().equals(kullanici.getId())) {
            return "redirect:/sahip";
        }

        // Bu mağazaya mesaj gönderen müşteriler
        List<Kullanici> musteriler = mesajService.getMagazaMusterileri(magazaId);

        // Seçili müşteri varsa sohbeti göster
        List<Mesaj> seciliSohbet = new ArrayList<>();
        Kullanici seciliMusteri = null;

        if (musteriId != null) {
            seciliMusteri = kullaniciService.getKullaniciById(musteriId);
            seciliSohbet = mesajService.getMagazaMusteriSohbet(magazaId, musteriId, true);
        }

        model.addAttribute("kullanici", kullanici);
        model.addAttribute("magaza", magaza);
        model.addAttribute("musteriler", musteriler);
        model.addAttribute("seciliMusteri", seciliMusteri);
        model.addAttribute("seciliSohbet", seciliSohbet);

        return "sahip/magaza-mesajlar";
    }
}
