package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.UserEntity;
import com.genie.Drive_BE.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 *  GET   /api/settings          → get settings
 *  PATCH /api/settings          → update theme, notifications
 *  PATCH /api/settings/ai       → update AI provider + key
 */
@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "http://localhost:3000")
public class SettingsController {

    private final UserService userService;

    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserEntity> getSettings() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PatchMapping
    public ResponseEntity<UserEntity> updateSettings(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(userService.updateSettings(
                (String)  body.get("theme"),
                body.get("emailNotifications")  != null ? (Boolean) body.get("emailNotifications")  : null,
                body.get("uploadNotifications") != null ? (Boolean) body.get("uploadNotifications") : null,
                body.get("shareNotifications")  != null ? (Boolean) body.get("shareNotifications")  : null
        ));
    }

    @PatchMapping("/ai")
    public ResponseEntity<UserEntity> updateAiSettings(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                userService.updateAiSettings(body.get("aiProvider"), body.get("aiApiKey"))
        );
    }
}
