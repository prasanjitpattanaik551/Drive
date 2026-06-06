package com.genie.Drive_BE.services;

import com.genie.Drive_BE.entity.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class AiService {

    @Value("${ai.gemini.api-key:}")
    private String defaultGeminiKey;

    @Value("${ai.openai.api-key:}")
    private String defaultOpenAiKey;

    private final AuthService  authService;
    private final RestTemplate restTemplate = new RestTemplate();

    public AiService(AuthService authService) {
        this.authService = authService;
    }

    public String chat(String userMessage, String fileContext) {
        UserEntity user     = authService.getLoggedInUser();
        String     provider = user.getAiProvider() != null ? user.getAiProvider() : "gemini";
        String     apiKey   = (user.getAiApiKey() != null && !user.getAiApiKey().isBlank())
                ? user.getAiApiKey()
                : ("openai".equals(provider) ? defaultOpenAiKey : defaultGeminiKey);

        String prompt = (fileContext != null && !fileContext.isBlank())
                ? "Context: " + fileContext + "\n\nQuestion: " + userMessage
                : userMessage;

        if (apiKey == null || apiKey.isBlank()) {
            return "No AI API key configured. Go to Settings → AI to add your key.";
        }

        return "openai".equals(provider)
                ? callOpenAi(prompt, apiKey)
                : callGemini(prompt, apiKey);
    }

    // ── Gemini — tries models in order until one works ─────────────────────
    private String callGemini(String prompt, String apiKey) {

        // Current working Gemini models (updated May 2025)
        // Try each one — if quota exceeded or not found, try next
        String[] models = {
                "gemini-2.0-flash-lite",   // cheapest, most quota
                "gemini-1.5-flash-latest", // stable alias
                "gemini-1.5-pro-latest",   // fallback
        };

        for (String model : models) {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + apiKey;

            Map<String, Object> part    = Map.of("text", prompt);
            Map<String, Object> content = Map.of("parts", List.of(part));
            Map<String, Object> body    = Map.of("contents", List.of(content));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            try {
                ResponseEntity<Map> res = restTemplate.exchange(
                        url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

                if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> candidates =
                            (List<Map<String, Object>>) res.getBody().get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> c = (Map<String, Object>) candidates.get(0).get("content");
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) c.get("parts");
                        return (String) parts.get(0).get("text");
                    }
                }
            } catch (Exception e) {
                String msg = e.getMessage();
                // 404 = model not found, try next
                // 429 = quota exceeded, try next model
                if (msg != null && (msg.contains("404") || msg.contains("429"))) {
                    continue; // try next model
                }
                return "Gemini error: " + msg;
            }
        }

        return "All Gemini models are currently unavailable or quota exceeded. " +
                "Please try again later or switch to OpenAI in Settings → AI.";
    }

    // ── OpenAI ────────────────────────────────────────────────────────────
    private String callOpenAi(String prompt, String apiKey) {
        String url = "https://api.openai.com/v1/chat/completions";

        Map<String, Object> body = Map.of(
                "model",    "gpt-4o-mini",
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            ResponseEntity<Map> res = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices =
                    (List<Map<String, Object>>) res.getBody().get("choices");
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
            return (String) msg.get("content");

        } catch (Exception e) {
            return "OpenAI error: " + e.getMessage();
        }
    }
}