package com.genie.Drive_BE.services;

import com.genie.Drive_BE.entity.UserEntity;
import com.genie.Drive_BE.repo.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService   emailService;

    public AuthService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService   = emailService;
    }

    private HttpServletRequest getRequest() {
        return ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    // ── Step 1: Send OTP ──────────────────────────────────────────────────
    public String sendOtp(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            UserEntity u = new UserEntity();
            u.setEmail(email);
            u.setName(email.split("@")[0]);
            u.setAvatarColor(randomColor());
            return u;
        });
        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        emailService.sendOtp(email, otp);
        return "OTP sent to " + email;
    }

    // ── Step 2: Verify OTP → generate token → save in DB ─────────────────
    public UserEntity verifyOtp(String email, String otp) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp))
            throw new RuntimeException("Invalid OTP");

        if (LocalDateTime.now().isAfter(user.getOtpExpiresAt()))
            throw new RuntimeException("OTP expired. Please request a new one.");

        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        user.setVerified(true);

        String token = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        user.setSessionToken(token);

        UserEntity saved = userRepository.save(user);
        System.out.println("✅ TOKEN GENERATED for " + email + ": " + token.substring(0, 8) + "...");
        return saved;
    }

    // ── Logout ────────────────────────────────────────────────────────────
    public void logout() {
        try {
            String token = getRequest().getHeader("X-Auth-Token");
            if (token != null && !token.isBlank()) {
                userRepository.findBySessionToken(token).ifPresent(u -> {
                    u.setSessionToken(null);
                    userRepository.save(u);
                });
            }
        } catch (Exception ignored) {}
    }

    // ── Get logged-in user from X-Auth-Token header ───────────────────────
    public UserEntity getLoggedInUser() {
        String token = getRequest().getHeader("X-Auth-Token");

        // Debug: print what token arrived (first 8 chars)
        System.out.println("🔑 X-Auth-Token received: " +
                (token == null ? "NULL ← frontend not sending header!" :
                        token.isBlank() ? "BLANK" :
                        token.substring(0, Math.min(8, token.length())) + "..."));

        if (token == null || token.isBlank())
            throw new RuntimeException("Not logged in");

        return userRepository.findBySessionToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired session"));
    }

    public boolean isLoggedIn() {
        try { getLoggedInUser(); return true; }
        catch (Exception e) { return false; }
    }

    private String generateOtp() {
        return String.valueOf(new SecureRandom().nextInt(900000) + 100000);
    }

    private String randomColor() {
        String[] colors = {"#1A73E8","#34A853","#EA4335","#9C27B0","#FF5722","#00BCD4"};
        return colors[new SecureRandom().nextInt(colors.length)];
    }
}
