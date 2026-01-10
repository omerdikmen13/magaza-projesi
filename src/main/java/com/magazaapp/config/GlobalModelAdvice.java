package com.magazaapp.config;

import com.magazaapp.repository.KullaniciRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

/**
 * Global advice that adds the current user to all view models.
 * This ensures the navbar can properly show role-based menus.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final KullaniciRepository kullaniciRepository;

    public GlobalModelAdvice(KullaniciRepository kullaniciRepository) {
        this.kullaniciRepository = kullaniciRepository;
    }

    @ModelAttribute
    public void addKullaniciToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            kullaniciRepository.findByKullaniciAdi(username).ifPresent(kullanici -> {
                model.addAttribute("kullanici", kullanici);
            });
        }
    }
}
