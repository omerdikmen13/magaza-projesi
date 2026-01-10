package com.magazaapp.controller;

import com.magazaapp.model.*;
import com.magazaapp.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class MesajController {

    private final MesajRepository mesajRepository;
    private final KullaniciRepository kullaniciRepository;
    private final MagazaRepository magazaRepository;

    public MesajController(MesajRepository mesajRepository, KullaniciRepository kullaniciRepository,
            MagazaRepository magazaRepository) {
        this.mesajRepository = mesajRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.magazaRepository = magazaRepository;
    }

    // ============ MÜŞTERİ: MESAJLARIM ============
    @GetMapping("/mesajlarim")
    public String mesajlarim(Authentication auth, Model model) {
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Bu müşterinin sohbetlerini bul
        List<Mesaj> mesajlar = mesajRepository.findByMusteriIdOrderByTarihDesc(kullanici.getId());

        // Mağaza bazlı grupla (son mesaj)
        Map<Long, Mesaj> magazaSonMesaj = new LinkedHashMap<>();
        for (Mesaj m : mesajlar) {
            magazaSonMesaj.putIfAbsent(m.getMagaza().getId(), m);
        }

        model.addAttribute("kullanici", kullanici);
        model.addAttribute("sohbetler", magazaSonMesaj.values());

        return "mesajlarim";
    }

    // ============ MÜŞTERİ: SOHBET SAYFASI ============
    @GetMapping("/sohbet/{magazaId}")
    public String sohbet(@PathVariable Long magazaId, Authentication auth, Model model) {
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Magaza magaza = magazaRepository.findById(magazaId)
                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

        // Bu müşterinin bu mağaza ile mesajları
        List<Mesaj> mesajlar = mesajRepository.findByMagazaIdAndMusteriIdOrderByTarihAsc(magazaId, kullanici.getId());

        // Okunmamış mesajları okundu yap (mağaza sahibinden gelen)
        for (Mesaj m : mesajlar) {
            if (!m.getGonderenMusteri() && !m.getOkundu()) {
                m.setOkundu(true);
                mesajRepository.save(m);
            }
        }

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

        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Magaza magaza = magazaRepository.findById(magazaId)
                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

        // Mağaza sahibi mi müşteri mi?
        boolean gonderenMusteri = !magaza.getSahip().getId().equals(kullanici.getId());

        Mesaj mesaj = new Mesaj();
        mesaj.setGonderen(kullanici);
        mesaj.setMagaza(magaza);
        mesaj.setIcerik(icerik);
        mesaj.setGonderenMusteri(gonderenMusteri);

        if (gonderenMusteri) {
            // Müşteri gönderiyor - müşteri kendisi
            mesaj.setMusteri(kullanici);
            mesajRepository.save(mesaj);
            return "redirect:/sohbet/" + magazaId;
        } else {
            // Mağaza sahibi gönderiyor - müşteriyi bul
            if (musteriId != null) {
                Kullanici musteri = kullaniciRepository.findById(musteriId).orElse(null);
                mesaj.setMusteri(musteri);
                mesajRepository.save(mesaj);
                return "redirect:/sahip/mesajlar/" + magazaId + "?musteriId=" + musteriId;
            }
            return "redirect:/sahip/mesajlar/" + magazaId;
        }
    }

    // ============ MAĞAZA SAHİBİ: MESAJLAR ============
    @GetMapping("/sahip/mesajlar")
    public String sahipMesajlar(Authentication auth, Model model) {
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        // Sahibin mağazaları
        List<Magaza> magazalar = magazaRepository.findBySahipId(kullanici.getId());

        // Her mağaza için mesaj sayısı
        Map<Long, Long> okunmamisSayisi = new HashMap<>();

        for (Magaza m : magazalar) {
            okunmamisSayisi.put(m.getId(), mesajRepository.countOkunmamisByMagazaId(m.getId()));
        }

        model.addAttribute("kullanici", kullanici);
        model.addAttribute("magazalar", magazalar);
        model.addAttribute("okunmamisSayisi", okunmamisSayisi);

        return "sahip/mesajlar";
    }

    // ============ MAĞAZA SAHİBİ: BELİRLİ MAĞAZA MESAJLARI ============
    @GetMapping("/sahip/mesajlar/{magazaId}")
    public String sahipMagazaMesajlari(@PathVariable Long magazaId,
            @RequestParam(required = false) Long musteriId,
            Authentication auth, Model model) {

        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Magaza magaza = magazaRepository.findById(magazaId)
                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

        // Yetki kontrolü
        if (!magaza.getSahip().getId().equals(kullanici.getId())) {
            return "redirect:/sahip";
        }

        // Bu mağazaya mesaj gönderen müşteriler
        List<Kullanici> musteriler = mesajRepository.findDistinctMusterilerByMagazaId(magazaId);

        // Seçili müşteri varsa sohbeti göster
        List<Mesaj> seciliSohbet = new ArrayList<>();
        Kullanici seciliMusteri = null;

        if (musteriId != null) {
            seciliMusteri = kullaniciRepository.findById(musteriId).orElse(null);
            if (seciliMusteri != null) {
                seciliSohbet = mesajRepository.findByMagazaIdAndMusteriIdOrderByTarihAsc(magazaId, musteriId);

                // Okunmamış mesajları okundu yap
                for (Mesaj m : seciliSohbet) {
                    if (m.getGonderenMusteri() && !m.getOkundu()) {
                        m.setOkundu(true);
                        mesajRepository.save(m);
                    }
                }
            }
        }

        model.addAttribute("kullanici", kullanici);
        model.addAttribute("magaza", magaza);
        model.addAttribute("musteriler", musteriler);
        model.addAttribute("seciliMusteri", seciliMusteri);
        model.addAttribute("seciliSohbet", seciliSohbet);

        return "sahip/magaza-mesajlar";
    }
}
