package com.magazaapp.controller;

import com.magazaapp.model.Kullanici;
import com.magazaapp.model.SiparisFisi;
import com.magazaapp.model.SiparisDetay;
import com.magazaapp.repository.KullaniciRepository;
import com.magazaapp.repository.SiparisFisiRepository;
import com.magazaapp.repository.SiparisDetayRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SiparisController {

    private final SiparisFisiRepository siparisFisiRepository;
    private final SiparisDetayRepository siparisDetayRepository;
    private final KullaniciRepository kullaniciRepository;

    public SiparisController(SiparisFisiRepository siparisFisiRepository,
            SiparisDetayRepository siparisDetayRepository,
            KullaniciRepository kullaniciRepository) {
        this.siparisFisiRepository = siparisFisiRepository;
        this.siparisDetayRepository = siparisDetayRepository;
        this.kullaniciRepository = kullaniciRepository;
    }

    // ============ SİPARİŞLERİM ============
    @GetMapping("/siparislerim")
    public String siparislerim(Authentication auth, Model model) {
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        List<SiparisFisi> siparisler = siparisFisiRepository.findByKullaniciId(kullanici.getId());

        model.addAttribute("siparisler", siparisler);

        return "siparislerim";
    }

    // ============ SİPARİŞ DETAY ============
    @GetMapping("/siparis/{id}")
    public String siparisDetay(@PathVariable Long id, Authentication auth, Model model) {
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        SiparisFisi siparis = siparisFisiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sipariş bulunamadı"));

        // Güvenlik kontrolü: Sadece kendi siparişlerini görebilir
        if (!siparis.getKullanici().getId().equals(kullanici.getId())) {
            throw new RuntimeException("Bu siparişi görme yetkiniz yok!");
        }

        List<SiparisDetay> detaylar = siparisDetayRepository.findBySiparisFisiId(id);

        model.addAttribute("siparis", siparis);
        model.addAttribute("detaylar", detaylar);

        return "siparis-detay";
    }
}
