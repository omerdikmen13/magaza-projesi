package com.magazaapp.controller;

import com.magazaapp.model.Kategori;
import com.magazaapp.model.Magaza;
import com.magazaapp.model.Urun;
import com.magazaapp.repository.KategoriRepository;
import com.magazaapp.repository.MagazaRepository;
import com.magazaapp.repository.UrunRepository;
import com.magazaapp.service.GeminiService;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final GeminiService geminiService;
    private final UrunRepository urunRepository;
    private final MagazaRepository magazaRepository;
    private final KategoriRepository kategoriRepository;

    public AIController(GeminiService geminiService, UrunRepository urunRepository,
            MagazaRepository magazaRepository, KategoriRepository kategoriRepository) {
        this.geminiService = geminiService;
        this.urunRepository = urunRepository;
        this.magazaRepository = magazaRepository;
        this.kategoriRepository = kategoriRepository;
    }

    /**
     * AI'dan öneri al - Veritabanındaki ürünleri, mağazaları ve kategorileri
     * kullanarak akıllı öneri yapar
     */
    @PostMapping("/oneri")
    public Map<String, Object> oneriAl(@RequestBody Map<String, String> request) {
        String soru = request.get("soru");

        if (soru == null || soru.trim().isEmpty()) {
            return Map.of("response", "Lütfen bir soru yazın.");
        }

        // Veritabanından verileri çek
        List<Urun> tumUrunler = urunRepository.findByAktifTrue();
        List<Magaza> tumMagazalar = magazaRepository.findByAktifTrue();
        List<Kategori> tumKategoriler = kategoriRepository.findAll();

        // Mağazaları formatlı metin haline getir
        String magazaListesi = tumMagazalar.stream()
                .map(m -> String.format("- MAGAZA_ID:%d | %s", m.getId(), m.getAd()))
                .collect(Collectors.joining("\n"));

        // Kategorileri formatlı metin haline getir
        String kategoriListesi = tumKategoriler.stream()
                .map(k -> String.format("- KATEGORI_ID:%d | %s", k.getId(), k.getAd()))
                .collect(Collectors.joining("\n"));

        // Ürünleri AI için formatlı metin haline getir
        String urunListesi = tumUrunler.stream()
                .limit(40) // Token limiti için max 40 ürün
                .map(u -> String.format("- URUN_ID:%d | %s | %.2f TL | Sezon:%s | Mağaza:%s (ID:%d) | Kategori:%s",
                        u.getId(),
                        u.getAd(),
                        u.getFiyat(),
                        u.getSezon() != null ? u.getSezon().name() : "TUM_SEZON",
                        u.getMagaza() != null ? u.getMagaza().getAd() : "Bilinmiyor",
                        u.getMagaza() != null ? u.getMagaza().getId() : 0,
                        u.getAltKategori() != null && u.getAltKategori().getKategori() != null
                                ? u.getAltKategori().getKategori().getAd()
                                : "Bilinmiyor"))
                .collect(Collectors.joining("\n"));

        // AI Prompt - Mağaza ve Kategori destekli
        String systemPrompt = """
                Sen MagazaApp E-Ticaret uygulamasının AI alışveriş asistanısın.

                ========== MAĞAZALAR ==========
                %s
                ===============================

                ========== KATEGORİLER ==========
                %s
                =================================

                ========== MEVCUT ÜRÜNLER ==========
                %s
                ====================================

                GÖREVIN:
                1. Kullanıcının sorusunu analiz et.
                2. Uygun linkleri oluştur.
                3. Samimi ve yardımsever bir dille Türkçe cevap ver.

                LINK FORMATLARI (ÇOK ÖNEMLİ - AYNEN KULLAN):

                ➤ Mağaza sorulursa (örn: "Mavi ürünleri", "Koton mağazası"):
                  [[MAGAZA:ID]] formatında link ver.
                  Örnek: Mavi mağazasının ürünlerini görmek için [[MAGAZA:1]] tıklayın.

                ➤ Kategori sorulursa (örn: "Kadın ürünleri", "Erkek kıyafetleri", "Çocuk giyim"):
                  [[KATEGORI:ID]] formatında link ver.
                  Örnek: Kadın ürünlerini görmek için [[KATEGORI:2]] tıklayın.

                ➤ Spesifik ürün önerildiğinde:
                  [[URUN:ID]] formatında link ver.
                  Örnek: Bu ürünü incelemek için [[URUN:5]] tıklayın.

                ÖRNEKLER:
                - "Mavi mağazasındaki ürünler" → Mavi mağazasına hoş geldiniz! Tüm ürünleri görmek için [[MAGAZA:1]] tıklayın.
                - "Kadın ürünleri göster" → Kadın kategorisindeki ürünleri görmek için [[KATEGORI:2]] tıklayın.
                - "Erkek gömlek öner" → Size şu gömlekleri önerebilirim: [[URUN:3]] Mavi Basic Gömlek

                KURALLAR:
                - Sadece yukarıdaki listelerden ID kullan, uydurma!
                - Mağaza adı geçince [[MAGAZA:ID]] kullan
                - Kategori adı geçince [[KATEGORI:ID]] kullan
                - Spesifik ürün önerirken [[URUN:ID]] kullan
                - Birden fazla link kullanabilirsin

                Kullanıcı Sorusu: %s
                """;

        String fullPrompt = String.format(systemPrompt, magazaListesi, kategoriListesi, urunListesi, soru);
        String cevap = geminiService.metinUret(fullPrompt);

        Map<String, Object> response = new HashMap<>();
        response.put("response", cevap);
        response.put("urunSayisi", tumUrunler.size());
        response.put("magazaSayisi", tumMagazalar.size());

        return response;
    }
}
