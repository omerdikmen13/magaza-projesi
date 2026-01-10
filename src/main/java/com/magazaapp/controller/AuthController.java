package com.magazaapp.controller;

import com.magazaapp.model.Kullanici;
import com.magazaapp.repository.KullaniciRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.magazaapp.security.CustomUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();

    public AuthController(KullaniciRepository kullaniciRepository, PasswordEncoder passwordEncoder,
            CustomUserDetailsService userDetailsService) {
        this.kullaniciRepository = kullaniciRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/giris")
    public String girisYap(@RequestParam String kullaniciAdi,
            @RequestParam String sifre,
            @RequestParam String rol,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        // Kullanıcıyı bul
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(kullaniciAdi).orElse(null);

        // Kullanıcı yoksa veya şifre yanlışsa
        if (kullanici == null || !passwordEncoder.matches(sifre, kullanici.getSifre())) {
            redirectAttributes.addAttribute("error", "true");
            return "redirect:/giris";
        }

        // Rol kontrolü - KRİTİK NOKTA
        // Formdan gelen rol (MUSTERI, MAGAZA_SAHIBI, ADMIN) ile veritabanındaki rol
        // eşleşmeli
        String dbRol = kullanici.getRol().name();

        // Admin her yerden girebilir mi? Hayır, kullanıcı sadece ilgili sekmeden
        // girmeli dedi.
        // Ama admin belki müşteri sekmesinden girmek isterse? Kullanıcı "musteri
        // girişinden admin girmesin" dedi.

        if (!dbRol.equals(rol)) {
            // Rol uyuşmazlığı
            redirectAttributes.addFlashAttribute("hata", "Bu panelden sadece " + rol.replace("_", " ")
                    + " yetkisine sahip kullanıcılar giriş yapabilir. Sizin rolünüz: " + dbRol);
            return "redirect:/giris";
        }

        // Giriş başarılı - Spring Security Context'i oluştur
        UserDetails userDetails = userDetailsService.loadUserByUsername(kullaniciAdi);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
                userDetails.getAuthorities());

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        // Yönlendirme
        if ("ADMIN".equals(dbRol)) {
            return "redirect:/admin";
        } else if ("MAGAZA_SAHIBI".equals(dbRol)) {
            return "redirect:/sahip";
        } else {
            return "redirect:/";
        }
    }
}
