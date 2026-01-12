package com.magazaapp.controller;

import com.magazaapp.model.Kullanici;
import com.magazaapp.service.AiOneriService;
import com.magazaapp.service.KullaniciService;
import com.magazaapp.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

        private final AiOneriService aiOneriService;
        private final KullaniciService kullaniciService;
        private final GeminiService geminiService;

        public AIController(AiOneriService aiOneriService,
                        KullaniciService kullaniciService,
                        GeminiService geminiService) {
                this.aiOneriService = aiOneriService;
                this.kullaniciService = kullaniciService;
                this.geminiService = geminiService;
        }

        /**
         * AI'dan metin üret ve kaydet
         */
        @PostMapping("/generate")
        public ResponseEntity<?> generateText(@RequestBody Map<String, String> request,
                        Authentication auth) {
                try {
                        String prompt = request.get("prompt");

                        if (prompt == null || prompt.trim().isEmpty()) {
                                return ResponseEntity.badRequest()
                                                .body(Map.of("error", "Prompt boş olamaz"));
                        }

                        Kullanici kullanici = null;
                        if (auth != null) {
                                kullanici = kullaniciService.getByUsername(auth.getName());
                        }

                        // Sistem prompt - AI'ya link formatını öğret
                        String systemPrompt = """
                                        Sen bir alışveriş asistanısın. Mağaza Sistemi platformunda kullanıcılara yardım ediyorsun.

                                        ÖNEMLİ: Ürün, mağaza veya kategori önerdiğinde MUTLAKA şu formatlarda link ver:
                                        - Ürün linki: [[URUN:ID]] (örnek: [[URUN:1]], [[URUN:5]])
                                        - Mağaza linki: [[MAGAZA:ID]] (örnek: [[MAGAZA:1]], [[MAGAZA:2]])
                                        - Kategori linki: [[KATEGORI:ID]] (örnek: [[KATEGORI:1]] Erkek, [[KATEGORI:2]] Kadın, [[KATEGORI:3]] Çocuk)

                                        Mağazalar: Mavi (ID:1), Koton (ID:2), LC Waikiki (ID:3), Zara (ID:4)
                                        Kategoriler: Erkek (ID:1), Kadın (ID:2), Çocuk (ID:3)

                                        Her zaman Türkçe cevap ver ve önerilerinle birlikte link kullan.
                                        """;

                        String fullPrompt = systemPrompt + "\n\nKullanıcı sorusu: " + prompt;

                        // AI'dan cevap al
                        String response = geminiService.metinUret(fullPrompt);

                        // Kullanıcı varsa, öneriyi kaydet
                        if (kullanici != null) {
                                aiOneriService.createOneri(kullanici, prompt);
                        }

                        return ResponseEntity.ok(Map.of(
                                        "response", response,
                                        "success", true));

                } catch (Exception e) {
                        return ResponseEntity.internalServerError()
                                        .body(Map.of(
                                                        "error", e.getMessage(),
                                                        "success", false));
                }
        }

        /**
         * Kullanıcının AI geçmişini getir
         */
        @GetMapping("/history")
        public ResponseEntity<?> getHistory(Authentication auth) {
                try {
                        if (auth == null) {
                                return ResponseEntity.status(401)
                                                .body(Map.of("error", "Giriş yapmanız gerekiyor"));
                        }

                        Kullanici kullanici = kullaniciService.getByUsername(auth.getName());
                        var oneriler = aiOneriService.getKullaniciOnerileri(kullanici.getId());

                        return ResponseEntity.ok(Map.of(
                                        "history", oneriler,
                                        "count", oneriler.size(),
                                        "success", true));

                } catch (Exception e) {
                        return ResponseEntity.internalServerError()
                                        .body(Map.of("error", e.getMessage()));
                }
        }

        /**
         * AI öneri geçmişini temizle
         */
        @DeleteMapping("/history")
        public ResponseEntity<?> clearHistory(Authentication auth) {
                try {
                        if (auth == null) {
                                return ResponseEntity.status(401)
                                                .body(Map.of("error", "Giriş yapmanız gerekiyor"));
                        }

                        Kullanici kullanici = kullaniciService.getByUsername(auth.getName());
                        aiOneriService.deleteKullaniciTumOnerileri(kullanici.getId());

                        return ResponseEntity.ok(Map.of(
                                        "message", "Geçmiş temizlendi",
                                        "success", true));

                } catch (Exception e) {
                        return ResponseEntity.internalServerError()
                                        .body(Map.of("error", e.getMessage()));
                }
        }
}
