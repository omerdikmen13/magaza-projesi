package com.magazaapp.controller;

import com.magazaapp.model.Kullanici;
import com.magazaapp.service.KullaniciService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
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

    private final KullaniciService kullaniciService;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();

    public AuthController(KullaniciService kullaniciService,
            CustomUserDetailsService userDetailsService) {
        this.kullaniciService = kullaniciService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/giris")
    public String girisYap(@RequestParam String kullaniciAdi,
            @RequestParam String sifre,
            @RequestParam String rol,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        try {
            // Service katmanında authentication yapılacak
            Kullanici kullanici = kullaniciService.authenticateUser(kullaniciAdi, sifre, rol);

            // Giriş başarılı - Spring Security Context'i oluştur
            UserDetails userDetails = userDetailsService.loadUserByUsername(kullaniciAdi);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,
                    userDetails.getPassword(),
                    userDetails.getAuthorities());

            SecurityContext context = securityContextHolderStrategy.createEmptyContext();
            context.setAuthentication(authentication);
            securityContextHolderStrategy.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            // Yönlendirme
            String dbRol = kullanici.getRol().name();
            if ("ADMIN".equals(dbRol)) {
                return "redirect:/admin";
            } else if ("MAGAZA_SAHIBI".equals(dbRol)) {
                return "redirect:/sahip";
            } else {
                return "redirect:/";
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("hata", e.getMessage());
            return "redirect:/giris";
        }
    }
}
