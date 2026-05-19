package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.UserEntity;
import com.genie.Drive_BE.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Email required"));
        try {
            return ResponseEntity.ok(Map.of("message", authService.sendOtp(email.trim().toLowerCase())));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Returns full UserEntity including sessionToken
    // Frontend saves user.sessionToken to localStorage
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp   = body.get("otp");
        if (email == null || otp == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Email and OTP required"));
        try {
            UserEntity user = authService.verifyOtp(email.trim().toLowerCase(), otp.trim());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // Reads X-Auth-Token header, clears token from DB
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        authService.logout();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    // Reads X-Auth-Token header, returns current user
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        try {
            return ResponseEntity.ok(authService.getLoggedInUser());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }
    }
}