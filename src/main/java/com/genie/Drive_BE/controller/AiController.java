package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.services.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api/ai")

public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        String message     = body.get("message");
        String fileContext = body.getOrDefault("fileContext", "");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("reply", "Message is empty"));
        }
        String reply = aiService.chat(message, fileContext);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}
