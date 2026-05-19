package com.genie.Drive_BE.services;

import com.genie.Drive_BE.entity.UserEntity;
import com.genie.Drive_BE.repo.UserRepository;
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

    private String callGemini(String prompt, String apiKey) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        Map<String, Object> part    = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> body    = Map.of("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<Map> res = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) res.getBody().get("candidates");
            @SuppressWarnings("unchecked")
            Map<String, Object> c     = (Map<String, Object>) candidates.get(0).get("content");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts = (List<Map<String, Object>>) c.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "Gemini error: " + e.getMessage();
        }
    }

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
            List<Map<String, Object>> choices = (List<Map<String, Object>>) res.getBody().get("choices");
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
            return (String) msg.get("content");
        } catch (Exception e) {
            return "OpenAI error: " + e.getMessage();
        }
    }
}
