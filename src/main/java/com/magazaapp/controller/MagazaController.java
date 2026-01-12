package com.magazaapp.controller;

import com.magazaapp.model.Kategori;
import com.magazaapp.model.Magaza;
import com.magazaapp.model.Urun;
import com.magazaapp.service.MagazaService;
import com.magazaapp.service.UrunService;
import com.magazaapp.service.KategoriService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/magazalar")
public class MagazaController {

    private final MagazaService magazaService;
    private final UrunService urunService;
    private final KategoriService kategoriService;

    public MagazaController(MagazaService magazaService, UrunService urunService,
            KategoriService kategoriService) {
        this.magazaService = magazaService;
        this.urunService = urunService;
        this.kategoriService = kategoriService;
    }

    // ============ MAĞAZA LİSTESİ (KATEGORİYE GÖRE FİLTRELİ) ============
    @GetMapping
    public String magazaListesi(@RequestParam(required = false) Long kategoriId, Model model) {
        List<Magaza> magazalar = magazaService.getTumMagazalar();
        List<Kategori> kategoriler = kategoriService.getTumKategoriler();

        if (kategoriId != null) {
            // Kategoriye göre ürünleri filtrele
            List<Urun> urunler = urunService.getUrunlerByKategori(kategoriId);
            model.addAttribute("urunler", urunler);
            model.addAttribute("secilenKategori", kategoriService.getKategoriById(kategoriId));
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
        Magaza magaza = magazaService.getMagazaById(id);

        List<Urun> urunler;
        if (kategoriId != null) {
            urunler = urunService.getUrunlerByMagazaAndKategori(id, kategoriId);
        } else {
            urunler = magazaService.getMagazaninUrunleri(id);
        }

        // Fiyat sıralaması
        if (siralama != null) {
            urunler = urunService.siralaUrunler(urunler, siralama);
        }

        List<Kategori> kategoriler = kategoriService.getTumKategoriler();

        model.addAttribute("magaza", magaza);
        model.addAttribute("urunler", urunler);
        model.addAttribute("kategoriler", kategoriler);
        model.addAttribute("kategoriId", kategoriId);
        model.addAttribute("siralama", siralama);

        return "magaza-detay";
    }
}
