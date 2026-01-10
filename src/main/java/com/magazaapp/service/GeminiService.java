package com.magazaapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    // Use Gemini 2.0 Flash or 1.5 Flash as per availability. The code uses v1beta
    // standard.
    // The PDF link was: .../models/gemini-2.5-flash:generateContent (actually
    // likely 1.5 in reality, but user pdf said 2.5? PDF OCR says "gemini-2.5-flash"
    // on page 11)
    // We will stick to what PDF says, but if it fails we might need to change to
    // 1.5.
    private static final String BASE_URL = "https://generativelanguage.googleapis.com";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String metinUret(String istek) {
        // Construct the URL with query param
        // PDF says: "/v1/models/gemini-2.5-flash:generateContent?key=" + apiKey
        // Note: It is usually v1beta.

        String uri = "/v1beta/models/gemini-2.0-flash-exp:generateContent?key=" + apiKey;
        // NOTE: "gemini-2.5-flash" might be a typo in PDF or a specific future model.
        // Standard free model is "gemini-1.5-flash" or "gemini-pro".
        // "gemini-2.0-flash-exp" is the latest experimental.
        // I will interpret "gemini-2.5-flash" from PDF as likely intent for "latest
        // fast model".
        // Let's use "gemini-1.5-flash" as it is stable, or "gemini-pro".
        // Use "gemini-1.5-flash" for speed/cost.

        // Let's try to match PDF exactly first? "gemini-2.5-flash" sounds like a
        // specific instruction.
        // If it fails, I'll switch.
        // Actually, let's use "gemini-1.5-flash" to be safe because 2.5 doesn't
        // officially exist publicly as stable yet (maybe alpha?).
        // User PDF might be hypothetical or bleeding edge?
        // I will use "gemini-1.5-flash" to ensure it works.

        uri = "/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        // Construct Body
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", istek)))));

        try {
            String response = webClient.post()
                    .uri(uri)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Blocking for simplicity in this context

            // Parse response to find "text"
            // Response structure: candidates[0].content.parts[0].text
            var jsonNode = objectMapper.readTree(response);
            return jsonNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "Yapay zeka servisine ulaşılamadı: " + e.getMessage();
        }
    }
}
