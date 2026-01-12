package com.magazaapp.service;

import com.magazaapp.model.Kullanici;
import com.magazaapp.repository.KullaniciRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KullaniciService {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;

    public KullaniciService(KullaniciRepository kullaniciRepository,
            PasswordEncoder passwordEncoder) {
        this.kullaniciRepository = kullaniciRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Kullanıcı adına göre kullanıcı bul
     */
    public Kullanici getKullaniciByUsername(String kullaniciAdi) {
        return kullaniciRepository.findByKullaniciAdi(kullaniciAdi)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }

    /**
     * ID'ye göre kullanıcı bul
     */
    public Kullanici getKullaniciById(Long id) {
        return kullaniciRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }

    /**
     * Tüm kullanıcıları getir (Admin için)
     */
    public List<Kullanici> getTumKullanicilar() {
        return kullaniciRepository.findAll();
    }

    /**
     * Kullanıcı kaydet
     */
    @Transactional
    public Kullanici saveKullanici(Kullanici kullanici) {
        return kullaniciRepository.save(kullanici);
    }

    /**
     * Kullanıcı profil güncelle
     */
    @Transactional
    public Kullanici profilGuncelle(Long kullaniciId, String ad, String soyad,
            String email, String telefon, String adres) {
        Kullanici kullanici = getKullaniciById(kullaniciId);

        if (ad != null && !ad.trim().isEmpty()) {
            kullanici.setAd(ad);
        }
        if (soyad != null && !soyad.trim().isEmpty()) {
            kullanici.setSoyad(soyad);
        }
        if (email != null && !email.trim().isEmpty()) {
            kullanici.setEmail(email);
        }
        if (telefon != null) {
            kullanici.setTelefon(telefon);
        }
        if (adres != null) {
            kullanici.setAdres(adres);
        }

        return kullaniciRepository.save(kullanici);
    }

    /**
     * Şifre değiştir
     */
    @Transactional
    public void sifreDegistir(Long kullaniciId, String eskiSifre, String yeniSifre) {
        Kullanici kullanici = getKullaniciById(kullaniciId);

        // Eski şifre kontrolü
        if (!passwordEncoder.matches(eskiSifre, kullanici.getSifre())) {
            throw new RuntimeException("Eski şifre hatalı!");
        }

        // Yeni şifreyi encode et ve kaydet
        kullanici.setSifre(passwordEncoder.encode(yeniSifre));
        kullaniciRepository.save(kullanici);
    }

    /**
     * Kullanıcı sil
     */
    @Transactional
    public void deleteKullanici(Long id) {
        kullaniciRepository.deleteById(id);
    }

    /**
     * Kullanıcı adı varlık kontrolü
     */
    public boolean kullaniciAdiVarMi(String kullaniciAdi) {
        return kullaniciRepository.findByKullaniciAdi(kullaniciAdi).isPresent();
    }

    /**
     * Email varlık kontrolü
     */
    public boolean emailVarMi(String email) {
        return kullaniciRepository.findAll().stream()
                .anyMatch(k -> k.getEmail() != null && k.getEmail().equalsIgnoreCase(email));
    }

    /**
     * Username alias for getKullaniciByUsername
     */
    public Kullanici getByUsername(String username) {
        return getKullaniciByUsername(username);
    }

    /**
     * Yeni kullanıcı kaydı (Register)
     */
    @Transactional
    public Kullanici registerUser(String kullaniciAdi, String email, String sifre,
            String ad, String soyad, com.magazaapp.model.KullaniciRol rol) {
        // Kullanıcı adı kontrolü
        if (kullaniciAdiVarMi(kullaniciAdi)) {
            throw new RuntimeException("Bu kullanıcı adı zaten kayıtlı!");
        }

        // Email kontrolü
        if (kullaniciRepository.existsByEmail(email)) {
            throw new RuntimeException("Bu email zaten kullanılıyor!");
        }

        // Yeni kullanıcı oluştur
        Kullanici kullanici = new Kullanici();
        kullanici.setKullaniciAdi(kullaniciAdi);
        kullanici.setEmail(email);
        kullanici.setSifre(passwordEncoder.encode(sifre));
        kullanici.setAd(ad);
        kullanici.setSoyad(soyad);
        kullanici.setRol(rol);

        return kullaniciRepository.save(kullanici);
    }

    /**
     * Profil güncelleme (Controller için)
     */
    @Transactional
    public Kullanici updateProfile(Long kullaniciId, String ad, String soyad,
            String email, String telefon, String adres) {
        Kullanici kullanici = getKullaniciById(kullaniciId);

        // Email değişmişse ve başkası kullanıyorsa hata ver
        if (!kullanici.getEmail().equals(email) && kullaniciRepository.existsByEmail(email)) {
            throw new RuntimeException("Bu email adresi zaten kullanımda!");
        }

        kullanici.setAd(ad);
        kullanici.setSoyad(soyad);
        kullanici.setEmail(email);
        kullanici.setTelefon(telefon);
        kullanici.setAdres(adres);

        return kullaniciRepository.save(kullanici);
    }

    /**
     * Şifre değiştirme (Controller için - validation ile)
     */
    @Transactional
    public void changePassword(Long kullaniciId, String mevcutSifre,
            String yeniSifre, String yeniSifreTekrar) {
        Kullanici kullanici = getKullaniciById(kullaniciId);

        // Mevcut şifre kontrolü
        if (!passwordEncoder.matches(mevcutSifre, kullanici.getSifre())) {
            throw new RuntimeException("Mevcut şifreniz hatalı!");
        }

        // Yeni şifre kontrolü
        if (!yeniSifre.equals(yeniSifreTekrar)) {
            throw new RuntimeException("Yeni şifreler eşleşmiyor!");
        }

        if (yeniSifre.length() < 6) {
            throw new RuntimeException("Şifre en az 6 karakter olmalı!");
        }

        kullanici.setSifre(passwordEncoder.encode(yeniSifre));
        kullaniciRepository.save(kullanici);
    }

    /**
     * Kullanıcı authentication (login için)
     */
    public Kullanici authenticateUser(String kullaniciAdi, String sifre, String beklenenRol) {
        // Kullanıcıyı bul
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(kullaniciAdi)
                .orElse(null);

        // Kullanıcı yoksa veya şifre yanlışsa
        if (kullanici == null || !passwordEncoder.matches(sifre, kullanici.getSifre())) {
            throw new RuntimeException("Kullanıcı adı veya şifre hatalı!");
        }

        // Rol kontrolü
        String dbRol = kullanici.getRol().name();
        if (!dbRol.equals(beklenenRol)) {
            throw new RuntimeException("Bu panelden sadece " + beklenenRol.replace("_", " ")
                    + " yetkisine sahip kullanıcılar giriş yapabilir. Sizin rolünüz: " + dbRol);
        }

        return kullanici;
    }

    /**
     * Kullanıcı kaydet (alias for save)
     */
    @Transactional
    public Kullanici kaydet(Kullanici kullanici) {
        return kullaniciRepository.save(kullanici);
    }

    /**
     * Kullanıcı sil
     */
    @Transactional
    public void sil(Long id) {
        kullaniciRepository.deleteById(id);
    }
}
