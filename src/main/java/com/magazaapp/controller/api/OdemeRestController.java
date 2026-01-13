package com.magazaapp.controller.api;

import com.magazaapp.model.*;
import com.magazaapp.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * MOCK Ödeme REST API - React Native için
 * API key gerektirmez, simüle edilmiş ödeme
 */
@RestController
@RequestMapping("/api/odeme")
public class OdemeRestController {

    private final MockOdemeService mockOdemeService;
    private final KullaniciService kullaniciService;
    private final SiparisService siparisService;

    public OdemeRestController(MockOdemeService mockOdemeService, KullaniciService kullaniciService,
            SiparisService siparisService) {
        this.mockOdemeService = mockOdemeService;
        this.kullaniciService = kullaniciService;
        this.siparisService = siparisService;
    }

    /**
     * Ödeme başlat
     * POST /api/odeme/basla
     */
    @PostMapping("/basla")
    public ResponseEntity<?> odemeBaslat(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Authentication auth) {
        try {
            Kullanici kullanici = null;

            // Önce Spring Security Authentication'dan dene
            if (auth != null && auth.getName() != null) {
                kullanici = kullaniciService.getByUsername(auth.getName());
            }

            // Yoksa header'dan token ile dene (mobil app simple-token için)
            if (kullanici == null && authHeader != null) {
                String token = authHeader.replace("Bearer ", "").trim();
                // simple-token-{userId} formatından userId parse et
                if (token.startsWith("simple-token-")) {
                    try {
                        String userIdStr = token.replace("simple-token-", "");
                        Long userId = Long.parseLong(userIdStr);
                        kullanici = kullaniciService.getKullaniciById(userId);
                    } catch (Exception e) {
                        // Parse hatası, devam et
                    }
                }
            }

            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "error", "Giriş yapmanız gerekiyor"));
            }

            MockOdemeService.OdemeBaslatSonuc sonuc = mockOdemeService.odemeBaslat(kullanici);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", sonuc.getToken());
            response.put("odemeId", sonuc.getOdemeId());
            response.put("tutar", sonuc.getTutar());
            response.put("mock", true); // Mock olduğunu belirt

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    /**
     * Ödeme tamamla - Kart bilgileri ile
     * POST /api/odeme/tamamla
     */
    @PostMapping("/tamamla")
    public ResponseEntity<?> odemeTamamla(
            @RequestBody OdemeTamamlaRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            Authentication auth) {
        try {
            MockOdemeService.OdemeSonuc sonuc = mockOdemeService.odemeTamamla(
                    request.getToken(),
                    request.getKartNo(),
                    request.getSonKullanma(),
                    request.getCvv(),
                    request.getKartSahibi());

            if (sonuc.isBasarili()) {
                Kullanici kullanici = null;

                // Önce Spring Security Authentication'dan dene
                if (auth != null && auth.getName() != null) {
                    kullanici = kullaniciService.getByUsername(auth.getName());
                }

                // Yoksa header'dan token ile dene (mobil app simple-token için)
                if (kullanici == null && authHeader != null) {
                    String token = authHeader.replace("Bearer ", "").trim();
                    if (token.startsWith("simple-token-")) {
                        try {
                            String userIdStr = token.replace("simple-token-", "");
                            Long userId = Long.parseLong(userIdStr);
                            kullanici = kullaniciService.getKullaniciById(userId);
                        } catch (Exception e) {
                            // Parse hatası
                        }
                    }
                }

                if (kullanici == null) {
                    return ResponseEntity.status(401).body(Map.of(
                            "success", false,
                            "error", "Giriş yapmanız gerekiyor"));
                }

                SiparisFisi siparis = siparisService.sepettenSiparisOlustur(kullanici.getId());

                Odeme odeme = sonuc.getOdeme();
                odeme.setSiparisFisi(siparis);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Ödeme başarılı!",
                        "siparisId", siparis.getId(),
                        "odemeId", odeme.getId()));
            } else {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", sonuc.getMesaj()));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    /**
     * Ödeme durumunu sorgula
     * GET /api/odeme/durum/{token}
     */
    @GetMapping("/durum/{token}")
    public ResponseEntity<?> odemeDurumu(@PathVariable String token) {
        try {
            Odeme odeme = mockOdemeService.getOdemeByToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("durum", odeme.getDurum().name());
            response.put("odemeId", odeme.getId());

            if (odeme.getSiparisFisi() != null) {
                response.put("siparisId", odeme.getSiparisFisi().getId());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    // ================= REQUEST CLASS =================

    public static class OdemeTamamlaRequest {
        private String token;
        private String kartNo;
        private String sonKullanma;
        private String cvv;
        private String kartSahibi;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getKartNo() {
            return kartNo;
        }

        public void setKartNo(String kartNo) {
            this.kartNo = kartNo;
        }

        public String getSonKullanma() {
            return sonKullanma;
        }

        public void setSonKullanma(String sonKullanma) {
            this.sonKullanma = sonKullanma;
        }

        public String getCvv() {
            return cvv;
        }

        public void setCvv(String cvv) {
            this.cvv = cvv;
        }

        public String getKartSahibi() {
            return kartSahibi;
        }

        public void setKartSahibi(String kartSahibi) {
            this.kartSahibi = kartSahibi;
        }
    }
}
