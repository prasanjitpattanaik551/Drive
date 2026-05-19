package com.genie.Drive_BE.services;

import com.genie.Drive_BE.entity.UserEntity;
import com.genie.Drive_BE.repo.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;

@Service
public class UserService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final UserRepository userRepository;
    private final AuthService    authService;

    public UserService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService    = authService;
    }

    public UserEntity getCurrentUser() {
        return authService.getLoggedInUser();
    }

    public UserEntity updateProfile(String name, String phone, String bio, String avatarColor) {
        UserEntity u = getCurrentUser();
        if (name        != null) u.setName(name);
        if (phone       != null) u.setPhone(phone);
        if (bio         != null) u.setBio(bio);
        if (avatarColor != null) u.setAvatarColor(avatarColor);
        return userRepository.save(u);
    }

    public UserEntity uploadAvatar(MultipartFile file) throws IOException {
        Path dir = Paths.get(uploadDir, "avatars");
        if (!Files.exists(dir)) Files.createDirectories(dir);
        String filename = "avatar_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path dest = dir.resolve(filename);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        UserEntity u = getCurrentUser();
        u.setAvatarUrl(dest.toString());
        return userRepository.save(u);
    }

    public UserEntity updateSettings(String theme, Boolean emailNotif,
                                     Boolean uploadNotif, Boolean shareNotif) {
        UserEntity u = getCurrentUser();
        if (theme       != null) u.setTheme(theme);
        if (emailNotif  != null) u.setEmailNotifications(emailNotif);
        if (uploadNotif != null) u.setUploadNotifications(uploadNotif);
        if (shareNotif  != null) u.setShareNotifications(shareNotif);
        return userRepository.save(u);
    }

    public UserEntity updateAiSettings(String provider, String apiKey) {
        UserEntity u = getCurrentUser();
        if (provider != null) u.setAiProvider(provider);
        if (apiKey   != null) u.setAiApiKey(apiKey);
        return userRepository.save(u);
    }
}
