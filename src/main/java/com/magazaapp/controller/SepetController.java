package com.magazaapp.controller;

import com.magazaapp.model.*;
import com.magazaapp.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class SepetController {

    private final SepetRepository sepetRepository;
    private final KullaniciRepository kullaniciRepository;
    private final SiparisFisiRepository siparisFisiRepository;
    private final SiparisDetayRepository siparisDetayRepository;
    private final UrunStokRepository urunStokRepository;

    public SepetController(SepetRepository sepetRepository, KullaniciRepository kullaniciRepository,
            SiparisFisiRepository siparisFisiRepository, SiparisDetayRepository siparisDetayRepository,
            UrunStokRepository urunStokRepository) {
        this.sepetRepository = sepetRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.siparisFisiRepository = siparisFisiRepository;
        this.siparisDetayRepository = siparisDetayRepository;
        this.urunStokRepository = urunStokRepository;
    }

    // ============ SEPET GÖSTER ============
    @GetMapping("/sepet")
    public String sepet(Authentication auth, Model model) {
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        List<Sepet> sepetListesi = sepetRepository.findByKullaniciId(kullanici.getId());

        BigDecimal toplam = sepetListesi.stream()
                .map(item -> item.getUrun().getFiyat().multiply(BigDecimal.valueOf(item.getAdet())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("sepetListesi", sepetListesi);
        model.addAttribute("toplam", toplam);
        model.addAttribute("kullanici", kullanici);

        return "sepet";
    }

    // ============ SEPETTEN SİL ============
    @PostMapping("/sepet/sil/{id}")
    public String sepettenSil(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        sepetRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("basari", "Ürün sepetten silindi!");
        return "redirect:/sepet";
    }

    // ============ SEPET GÜNCELLE ============
    @PostMapping("/sepet/guncelle/{id}")
    public String sepetGuncelle(@PathVariable Long id, @RequestParam int adet, RedirectAttributes redirectAttributes) {
        Sepet sepetItem = sepetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sepet öğesi bulunamadı"));

        if (adet <= 0) {
            sepetRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("basari", "Ürün sepetten silindi!");
        } else {
            // Stok kontrolü yapılmalı
            UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(
                    sepetItem.getUrun().getId(), sepetItem.getBeden().getId())
                    .orElseThrow(() -> new RuntimeException("Stok bulunamadı"));

            if (stok.getAdet() < adet) {
                redirectAttributes.addFlashAttribute("hata",
                        "Yetersiz stok! En fazla " + stok.getAdet() + " adet alabilirsiniz.");
            } else {
                sepetItem.setAdet(adet);
                sepetRepository.save(sepetItem);
                redirectAttributes.addFlashAttribute("basari", "Sepet güncellendi!");
            }
        }
        return "redirect:/sepet";
    }

    // ============ SEPETİ BOŞALT ============
    @PostMapping("/sepet/bosalt")
    @Transactional
    public String sepetiBosalt(Authentication auth, RedirectAttributes redirectAttributes) {
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        sepetRepository.deleteByKullaniciId(kullanici.getId());

        redirectAttributes.addFlashAttribute("basari", "Sepet boşaltıldı!");
        return "redirect:/sepet";
    }

    // ============ SİPARİŞ VER ============
    @PostMapping("/sepet/siparis-ver")
    @Transactional
    public String siparisVer(Authentication auth, RedirectAttributes redirectAttributes) {
        try {
            Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

            List<Sepet> sepetListesi = sepetRepository.findByKullaniciId(kullanici.getId());

            if (sepetListesi.isEmpty()) {
                redirectAttributes.addFlashAttribute("hata", "Sepetiniz boş!");
                return "redirect:/sepet";
            }

            // Mağaza ID'sini al (sepetteki ilk ürünün mağazası)
            Magaza magaza = sepetListesi.get(0).getUrun().getMagaza();

            // Farklı mağaza kontrolü - tüm ürünler aynı mağazadan olmalı
            for (Sepet sepetItem : sepetListesi) {
                if (!sepetItem.getUrun().getMagaza().getId().equals(magaza.getId())) {
                    redirectAttributes.addFlashAttribute("hata",
                            "Sipariş oluşturulamadı: Sepetinizde farklı mağazalardan ürünler var. Lütfen tek mağazadan sipariş verin.");
                    return "redirect:/sepet";
                }
            }

            // ÖNCELİKLİ STOK KONTROLÜ - Sipariş vermeden önce tüm stokları kontrol et
            for (Sepet sepetItem : sepetListesi) {
                UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(
                        sepetItem.getUrun().getId(), sepetItem.getBeden().getId())
                        .orElseThrow(() -> new RuntimeException("Stok bulunamadı: " + sepetItem.getUrun().getAd()));

                if (stok.getAdet() < sepetItem.getAdet()) {
                    redirectAttributes.addFlashAttribute("hata",
                            "Yetersiz stok! '" + sepetItem.getUrun().getAd() + "' ürününden " +
                                    stok.getAdet() + " adet kaldı, sepetinizde " + sepetItem.getAdet() + " adet var.");
                    return "redirect:/sepet";
                }
            }

            // Toplam tutarı hesapla
            BigDecimal toplamTutar = sepetListesi.stream()
                    .map(item -> item.getUrun().getFiyat().multiply(BigDecimal.valueOf(item.getAdet())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Sipariş fişi oluştur
            SiparisFisi siparisFisi = new SiparisFisi();
            siparisFisi.setKullanici(kullanici);
            siparisFisi.setMagaza(magaza);
            siparisFisi.setToplamTutar(toplamTutar);
            siparisFisi.setTeslimatAdresi(kullanici.getAdres() != null ? kullanici.getAdres() : "Adres yok");
            siparisFisi.setDurum(SiparisDurum.BEKLEMEDE);
            siparisFisi = siparisFisiRepository.save(siparisFisi);

            // Sipariş detaylarını oluştur ve stokları düş
            for (Sepet sepetItem : sepetListesi) {
                SiparisDetay detay = new SiparisDetay();
                detay.setSiparisFisi(siparisFisi);
                detay.setUrun(sepetItem.getUrun());
                detay.setBeden(sepetItem.getBeden());
                detay.setAdet(sepetItem.getAdet());
                detay.setBirimFiyat(sepetItem.getUrun().getFiyat());
                detay.setToplamFiyat(sepetItem.getUrun().getFiyat().multiply(BigDecimal.valueOf(sepetItem.getAdet())));
                siparisDetayRepository.save(detay);

                // Stok düş (güvenli - yukarıda kontrol edildi)
                UrunStok stok = urunStokRepository.findByUrunIdAndBedenId(
                        sepetItem.getUrun().getId(), sepetItem.getBeden().getId())
                        .orElseThrow(() -> new RuntimeException("Stok bulunamadı"));

                int yeniStok = stok.getAdet() - sepetItem.getAdet();
                if (yeniStok < 0) {
                    throw new RuntimeException("Stok hatası: Negatif stok oluşamaz!");
                }
                stok.setAdet(yeniStok);
                urunStokRepository.save(stok);
            }

            // Sepeti boşalt
            sepetRepository.deleteByKullaniciId(kullanici.getId());

            redirectAttributes.addFlashAttribute("basari",
                    "Siparişiniz başarıyla oluşturuldu! Sipariş No: #" + siparisFisi.getId());
            return "redirect:/siparislerim";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("hata", "Sipariş oluşturulurken hata: " + e.getMessage());
            return "redirect:/sepet";
        }
    }
}
