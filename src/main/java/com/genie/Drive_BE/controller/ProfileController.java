package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.UserEntity;
import com.genie.Drive_BE.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 *  GET   /api/profile          → get profile
 *  PATCH /api/profile          → update name, phone, bio, avatarColor
 *  POST  /api/profile/avatar   → upload profile picture
 */
@RestController
@RequestMapping("/api/profile")

public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserEntity> getProfile() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PatchMapping
    public ResponseEntity<UserEntity> updateProfile(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(userService.updateProfile(
                body.get("name"), body.get("phone"),
                body.get("bio"),  body.get("avatarColor")
        ));
    }

    @PostMapping("/avatar")
    public ResponseEntity<UserEntity> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(userService.uploadAvatar(file));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
