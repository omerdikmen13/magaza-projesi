package com.magazaapp.controller;

import com.magazaapp.model.Kategori;
import com.magazaapp.model.Magaza;
import com.magazaapp.model.Urun;
import com.magazaapp.repository.KategoriRepository;
import com.magazaapp.repository.MagazaRepository;
import com.magazaapp.repository.UrunRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/magazalar")
public class MagazaController {

    private final MagazaRepository magazaRepository;
    private final UrunRepository urunRepository;
    private final KategoriRepository kategoriRepository;

    public MagazaController(MagazaRepository magazaRepository, UrunRepository urunRepository,
            KategoriRepository kategoriRepository) {
        this.magazaRepository = magazaRepository;
        this.urunRepository = urunRepository;
        this.kategoriRepository = kategoriRepository;
    }

    // ============ MAĞAZA LİSTESİ (KATEGORİYE GÖRE FİLTRELİ) ============
    @GetMapping
    public String magazaListesi(@RequestParam(required = false) Long kategoriId, Model model) {
        List<Magaza> magazalar = magazaRepository.findByAktifTrue();
        List<Kategori> kategoriler = kategoriRepository.findAll();

        if (kategoriId != null) {
            // Kategoriİye göre ürünleri filtrele
            List<Urun> urunler = urunRepository.findByFiltre(null, kategoriId, null);
            model.addAttribute("urunler", urunler);
            model.addAttribute("secilenKategori", kategoriRepository.findById(kategoriId).orElse(null));
        }

        model.addAttribute("magazalar", magazalar);
        model.addAttribute("kategoriler", kategoriler);
        model.addAttribute("kategoriId", kategoriId);

        return "magazalar";
    }

    // ============ MAĞAZA DETAY (ÜRÜNLER) ============
    @GetMapping("/{id}")
    public String magazaDetay(@PathVariable Long id,
            @RequestParam(required = false) Long kategoriId,
            @RequestParam(required = false) String siralama,
            Model model) {
        Magaza magaza = magazaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mağaza bulunamadı"));

        List<Urun> urunler;
        if (kategoriId != null) {
            urunler = urunRepository.findByFiltre(id, kategoriId, null);
        } else {
            urunler = urunRepository.findByMagazaIdAndAktifTrue(id);
        }

        // Fiyat sıralaması
        if (siralama != null) {
            if ("fiyat-artan".equals(siralama)) {
                urunler.sort((a, b) -> a.getFiyat().compareTo(b.getFiyat()));
            } else if ("fiyat-azalan".equals(siralama)) {
                urunler.sort((a, b) -> b.getFiyat().compareTo(a.getFiyat()));
            }
        }

        List<Kategori> kategoriler = kategoriRepository.findAll();

        model.addAttribute("magaza", magaza);
        model.addAttribute("urunler", urunler);
        model.addAttribute("kategoriler", kategoriler);
        model.addAttribute("kategoriId", kategoriId);
        model.addAttribute("siralama", siralama);

        return "magaza-detay";
    }
}
