package com.magazaapp.service;

import com.magazaapp.dto.SiparisOzetiDTO;
import com.magazaapp.model.Kullanici;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Email Mikroservis Client
 * Spring Boot → Python FastAPI → Gmail SMTP
 * 
 * Bu servis Python mikroservisine REST API ile istek atar,
 * Python da Gmail'e bağlanıp gerçek mail gönderir.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final WebClient webClient;

    @Value("${email.microservice.url:http://localhost:8000}")
    private String emailServiceUrl;

    public EmailService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * HOŞGELDİN MAİLİ GÖNDERİMİ
     * Spring Boot → Python Microservice → Gmail
     */
    @Async
    public void hosgeldinMailiGonder(Kullanici kullanici) {
        try {
            if (kullanici.getEmail() == null || kullanici.getEmail().isEmpty()) {
                logger.warn("⚠️ Email adresi boş, mail gönderilmedi");
                return;
            }

            logger.info("📧 [PYTHON MICROSERVICE] Hoşgeldin maili gönderiliyor: {}", kullanici.getEmail());

            Map<String, Object> request = new HashMap<>();
            request.put("to", kullanici.getEmail());
            request.put("kullanici_adi", kullanici.getKullaniciAdi());
            request.put("ad", kullanici.getAd() != null ? kullanici.getAd() : kullanici.getKullaniciAdi());

            String response = webClient.post()
                    .uri(emailServiceUrl + "/api/email/welcome")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.info("✅ [PYTHON MICROSERVICE] Hoşgeldin maili gönderildi: {} - Response: {}",
                    kullanici.getEmail(), response);

        } catch (Exception e) {
            logger.error("❌ [PYTHON MICROSERVICE] Hoşgeldin maili gönderilemedi: {} - Hata: {}",
                    kullanici.getEmail(), e.getMessage());
        }
    }

    /**
     * SİPARİŞ ÖZETİ MAİLİ GÖNDERİMİ
     * Spring Boot → Python Microservice → Gmail
     */
    @Async
    public void siparisOzetiMailiGonder(SiparisOzetiDTO siparis) {
        try {
            if (siparis.getMusteriEmail() == null || siparis.getMusteriEmail().isEmpty()) {
                logger.warn("⚠️ Email adresi boş, mail gönderilmedi");
                return;
            }

            logger.info("📧 [PYTHON MICROSERVICE] Sipariş özeti maili gönderiliyor: {} - Sipariş #{}",
                    siparis.getMusteriEmail(), siparis.getSiparisId());

            // Sipariş kalemlerini Python formatına dönüştür
            List<Map<String, Object>> kalemler = siparis.getKalemler().stream()
                    .map(k -> {
                        Map<String, Object> kalem = new HashMap<>();
                        kalem.put("urun_ad", k.getUrunAd());
                        kalem.put("beden", k.getBeden());
                        kalem.put("adet", k.getAdet());
                        kalem.put("birim_fiyat", k.getBirimFiyat().doubleValue());
                        kalem.put("toplam_fiyat", k.getToplamFiyat().doubleValue());
                        return kalem;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> request = new HashMap<>();
            request.put("to", siparis.getMusteriEmail());
            request.put("siparis_id", siparis.getSiparisId());
            request.put("musteri_ad", siparis.getMusteriAd());
            request.put("magaza_ad", siparis.getMagazaAd());
            request.put("toplam_tutar", siparis.getToplamTutar().doubleValue());
            request.put("teslimat_adresi", siparis.getTeslimatAdresi());
            request.put("siparis_tarihi", siparis.getSiparisTarihi());
            request.put("kalemler", kalemler);

            String response = webClient.post()
                    .uri(emailServiceUrl + "/api/email/order")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.info("✅ [PYTHON MICROSERVICE] Sipariş özeti maili gönderildi: {} - Response: {}",
                    siparis.getMusteriEmail(), response);

        } catch (Exception e) {
            logger.error("❌ [PYTHON MICROSERVICE] Sipariş özeti maili gönderilemedi: {} - Hata: {}",
                    siparis.getMusteriEmail(), e.getMessage());
        }
    }

    /**
     * SİPARİŞ DURUMU DEĞİŞİKLİĞİ MAİLİ
     */
    @Async
    public void siparisDurumuMailiGonder(String email, String musteriAd, Long siparisId,
            String eskiDurum, String yeniDurum) {
        try {
            logger.info("📧 [PYTHON MICROSERVICE] Sipariş durumu maili gönderiliyor: {} - Sipariş #{}",
                    email, siparisId);

            Map<String, Object> request = new HashMap<>();
            request.put("to", email);
            request.put("subject", "📦 Sipariş Durumu Güncellendi - #" + siparisId);
            request.put("body", String.format(
                    "<h2>Merhaba %s,</h2><p>Sipariş #%d durumu değişti: <strong>%s → %s</strong></p>",
                    musteriAd, siparisId, eskiDurum, yeniDurum));
            request.put("is_html", true);

            String response = webClient.post()
                    .uri(emailServiceUrl + "/api/email/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.info("✅ [PYTHON MICROSERVICE] Sipariş durumu maili gönderildi: {}", email);

        } catch (Exception e) {
            logger.error("❌ [PYTHON MICROSERVICE] Sipariş durumu maili gönderilemedi: {} - Hata: {}",
                    email, e.getMessage());
        }
    }
}
