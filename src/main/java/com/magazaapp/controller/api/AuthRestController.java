package com.magazaapp.controller.api;

import com.magazaapp.model.Kullanici;
import com.magazaapp.model.KullaniciRol;
import com.magazaapp.repository.KullaniciRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthRestController {

    @Autowired
    private KullaniciRepository kullaniciRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =============== REGISTER ===============
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Kullanıcı adı kontrolü
            if (kullaniciRepository.existsByKullaniciAdi(request.getKullaniciAdi())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bu kullanıcı adı zaten kullanılıyor"));
            }

            // Email kontrolü
            if (request.getEmail() != null && kullaniciRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bu email zaten kullanılıyor"));
            }

            // Yeni kullanıcı oluştur
            Kullanici kullanici = new Kullanici();
            kullanici.setKullaniciAdi(request.getKullaniciAdi());
            kullanici.setEmail(request.getEmail());
            kullanici.setSifre(passwordEncoder.encode(request.getSifre()));
            kullanici.setAd(request.getAd());
            kullanici.setSoyad(request.getSoyad());
            kullanici.setTelefon(request.getTelefon());
            kullanici.setAdres(request.getAdres());
            kullanici.setAdres(request.getAdres());

            // Rol belirleme
            if (request.getRol() != null && !request.getRol().isEmpty()) {
                try {
                    KullaniciRol rol = KullaniciRol.valueOf(request.getRol());
                    if (rol == KullaniciRol.ADMIN) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Admin rolü ile kayıt olunamaz"));
                    }
                    kullanici.setRol(rol);
                } catch (IllegalArgumentException e) {
                    kullanici.setRol(KullaniciRol.MUSTERI);
                }
            } else {
                kullanici.setRol(KullaniciRol.MUSTERI);
            }

            kullaniciRepository.save(kullanici);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Kayıt başarılı");
            response.put("kullanici", createUserResponse(kullanici));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Kayıt sırasında hata: " + e.getMessage()));
        }
    }

    // =============== LOGIN ===============
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Optional<Kullanici> kullaniciOpt = kullaniciRepository.findByKullaniciAdi(request.getKullaniciAdi());

            if (kullaniciOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Kullanıcı bulunamadı"));
            }

            Kullanici kullanici = kullaniciOpt.get();

            // Şifre kontrolü
            if (!passwordEncoder.matches(request.getSifre(), kullanici.getSifre())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Şifre hatalı"));
            }

            // Aktif kontrolü
            if (!kullanici.getAktif()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Hesabınız aktif değil"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Giriş başarılı");
            response.put("kullanici", createUserResponse(kullanici));
            response.put("token", "simple-token-" + kullanici.getId()); // Basit token

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Giriş sırasında hata: " + e.getMessage()));
        }
    }

    // =============== PROFIL GÜNCELLE ===============
    @PutMapping("/profil")
    public ResponseEntity<?> profilGuncelle(@RequestHeader("Authorization") String token,
            @RequestBody ProfilRequest request) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            if (request.getAd() != null)
                kullanici.setAd(request.getAd());
            if (request.getSoyad() != null)
                kullanici.setSoyad(request.getSoyad());
            if (request.getEmail() != null)
                kullanici.setEmail(request.getEmail());
            if (request.getTelefon() != null)
                kullanici.setTelefon(request.getTelefon());
            if (request.getAdres() != null)
                kullanici.setAdres(request.getAdres());

            kullaniciRepository.save(kullanici);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Profil güncellendi");
            response.put("kullanici", createUserResponse(kullanici));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Profil güncellenirken hata: " + e.getMessage()));
        }
    }

    // =============== ŞİFRE DEĞİŞTİR ===============
    @PutMapping("/sifre-degistir")
    public ResponseEntity<?> sifreDegistir(@RequestHeader("Authorization") String token,
            @RequestBody SifreRequest request) {
        try {
            Kullanici kullanici = getKullaniciFromToken(token);
            if (kullanici == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Geçersiz token"));
            }

            // Mevcut şifreyi kontrol et
            if (!passwordEncoder.matches(request.getMevcutSifre(), kullanici.getSifre())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mevcut şifre yanlış"));
            }

            // Yeni şifre kontrolü
            if (request.getYeniSifre() == null || request.getYeniSifre().length() < 4) {
                return ResponseEntity.badRequest().body(Map.of("error", "Yeni şifre en az 4 karakter olmalı"));
            }

            kullanici.setSifre(passwordEncoder.encode(request.getYeniSifre()));
            kullaniciRepository.save(kullanici);

            return ResponseEntity.ok(Map.of("message", "Şifre başarıyla değiştirildi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Şifre değiştirirken hata: " + e.getMessage()));
        }
    }

    // =============== HELPER METHODS ===============
    private Kullanici getKullaniciFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        String tokenValue = token.substring(7);
        if (tokenValue.startsWith("simple-token-")) {
            try {
                Long userId = Long.parseLong(tokenValue.replace("simple-token-", ""));
                return kullaniciRepository.findById(userId).orElse(null);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    // =============== HELPER METHODS ===============
    private Map<String, Object> createUserResponse(Kullanici kullanici) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", kullanici.getId());
        userMap.put("kullaniciAdi", kullanici.getKullaniciAdi());
        userMap.put("email", kullanici.getEmail());
        userMap.put("ad", kullanici.getAd());
        userMap.put("soyad", kullanici.getSoyad());
        userMap.put("telefon", kullanici.getTelefon());
        userMap.put("adres", kullanici.getAdres());
        userMap.put("rol", kullanici.getRol().toString());
        return userMap;
    }

    // =============== REQUEST CLASSES ===============
    static class LoginRequest {
        private String kullaniciAdi;
        private String sifre;

        public String getKullaniciAdi() {
            return kullaniciAdi;
        }

        public void setKullaniciAdi(String kullaniciAdi) {
            this.kullaniciAdi = kullaniciAdi;
        }

        public String getSifre() {
            return sifre;
        }

        public void setSifre(String sifre) {
            this.sifre = sifre;
        }
    }

    static class RegisterRequest {
        private String kullaniciAdi;
        private String email;
        private String sifre;
        private String ad;
        private String soyad;
        private String telefon;
        private String adres;

        public String getKullaniciAdi() {
            return kullaniciAdi;
        }

        public void setKullaniciAdi(String kullaniciAdi) {
            this.kullaniciAdi = kullaniciAdi;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getSifre() {
            return sifre;
        }

        public void setSifre(String sifre) {
            this.sifre = sifre;
        }

        public String getAd() {
            return ad;
        }

        public void setAd(String ad) {
            this.ad = ad;
        }

        public String getSoyad() {
            return soyad;
        }

        public void setSoyad(String soyad) {
            this.soyad = soyad;
        }

        public String getTelefon() {
            return telefon;
        }

        public void setTelefon(String telefon) {
            this.telefon = telefon;
        }

        public String getAdres() {
            return adres;
        }

        public void setAdres(String adres) {
            this.adres = adres;
        }

        private String rol;

        public String getRol() {
            return rol;
        }

        public void setRol(String rol) {
            this.rol = rol;
        }
    }

    static class ProfilRequest {
        private String ad;
        private String soyad;
        private String email;
        private String telefon;
        private String adres;

        public String getAd() {
            return ad;
        }

        public void setAd(String ad) {
            this.ad = ad;
        }

        public String getSoyad() {
            return soyad;
        }

        public void setSoyad(String soyad) {
            this.soyad = soyad;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTelefon() {
            return telefon;
        }

        public void setTelefon(String telefon) {
            this.telefon = telefon;
        }

        public String getAdres() {
            return adres;
        }

        public void setAdres(String adres) {
            this.adres = adres;
        }
    }

    static class SifreRequest {
        private String mevcutSifre;
        private String yeniSifre;

        public String getMevcutSifre() {
            return mevcutSifre;
        }

        public void setMevcutSifre(String mevcutSifre) {
            this.mevcutSifre = mevcutSifre;
        }

        public String getYeniSifre() {
            return yeniSifre;
        }

        public void setYeniSifre(String yeniSifre) {
            this.yeniSifre = yeniSifre;
        }
    }
}
