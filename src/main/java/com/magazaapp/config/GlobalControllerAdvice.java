package com.magazaapp.config;

import com.magazaapp.model.Kullanici;
import com.magazaapp.repository.KullaniciRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final KullaniciRepository kullaniciRepository;

    public GlobalControllerAdvice(KullaniciRepository kullaniciRepository) {
        this.kullaniciRepository = kullaniciRepository;
    }

    @ModelAttribute("kullanici")
    public Kullanici getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return kullaniciRepository.findByKullaniciAdi(auth.getName()).orElse(null);
        }
        return null;
    }
}
