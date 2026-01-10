package com.magazaapp.config;

import com.magazaapp.model.Kullanici;
import com.magazaapp.model.KullaniciRol;
import com.magazaapp.repository.KullaniciRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final KullaniciRepository kullaniciRepository;

    public CustomLoginSuccessHandler(KullaniciRepository kullaniciRepository) {
        this.kullaniciRepository = kullaniciRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(username).orElse(null);

        String redirectUrl = "/"; // Default

        if (kullanici != null) {
            if (kullanici.getRol() == KullaniciRol.ADMIN) {
                redirectUrl = "/admin"; // Admin paneli
            } else if (kullanici.getRol() == KullaniciRol.MAGAZA_SAHIBI) {
                redirectUrl = "/sahip"; // Mağaza sahibi paneli
            } else {
                redirectUrl = "/"; // Müşteri ana sayfa
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
