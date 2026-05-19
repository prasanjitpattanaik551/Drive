package com.genie.Drive_BE.services;

import com.genie.Drive_BE.dto.StorageInfoDTO;
import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.entity.UserEntity;
import com.genie.Drive_BE.repo.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileServiceStorage {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${storage.limit-bytes:16106127360}")
    private long storageLimitBytes;

    private final FileRepository fileRepository;
    private final AuthService    authService;

    public FileServiceStorage(FileRepository fileRepository, AuthService authService) {
        this.fileRepository = fileRepository;
        this.authService    = authService;
    }

    // ── Get current logged-in user (throws if not logged in) ─────────────
    private UserEntity currentUser() {
        return authService.getLoggedInUser();
    }

    // ── Upload ────────────────────────────────────────────────────────────
    public String saveFile(MultipartFile file, Long parentFolderId) throws IOException {
        UserEntity user = currentUser();

        // Store each user's files in their own subdirectory
        Path userDir = Paths.get(uploadDir, "user_" + user.getId());
        if (!Files.exists(userDir)) Files.createDirectories(userDir);

        String fileName = file.getOriginalFilename();
        Path   filePath = userDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        FileEntity entity = new FileEntity();
        entity.setUserId(user.getId());         // ← bind to this user
        entity.setName(fileName);
        entity.setPath(filePath.toString());
        entity.setSize(file.getSize());
        entity.setType("file");
        entity.setParentFolderId(parentFolderId);
        entity.setCreatedAt(LocalDateTime.now());

        fileRepository.save(entity);
        return "File uploaded successfully!";
    }

    // ── My Drive list ─────────────────────────────────────────────────────
    public List<FileEntity> getFilesInFolder(Long parentFolderId) {
        Long uid = currentUser().getId();
        if (parentFolderId == null)
            return fileRepository.findByUserIdAndParentFolderIdIsNullAndTrashedFalseAndSpamFalse(uid);
        return fileRepository.findByUserIdAndParentFolderIdAndTrashedFalseAndSpamFalse(uid, parentFolderId);
    }

    // ── Download / mark opened ────────────────────────────────────────────
    public FileEntity markAsOpened(Long fileId) {
        FileEntity f = getFileByIdSecure(fileId);
        f.setLastOpenedAt(LocalDateTime.now());
        return fileRepository.save(f);
    }

    // ── Recent ────────────────────────────────────────────────────────────
    public List<FileEntity> getRecentFiles() {
        return fileRepository
                .findTop50ByUserIdAndLastOpenedAtIsNotNullAndTrashedFalseOrderByLastOpenedAtDesc(currentUser().getId());
    }

    // ── Starred ───────────────────────────────────────────────────────────
    public FileEntity toggleStar(Long fileId) {
        FileEntity f = getFileByIdSecure(fileId);
        f.setStarred(!f.isStarred());
        return fileRepository.save(f);
    }

    public List<FileEntity> getStarredFiles() {
        return fileRepository.findByUserIdAndStarredTrueAndTrashedFalseAndSpamFalse(currentUser().getId());
    }

    // ── Bin ───────────────────────────────────────────────────────────────
    public FileEntity moveToTrash(Long fileId) {
        FileEntity f = getFileByIdSecure(fileId);
        f.setTrashed(true);
        f.setTrashedAt(LocalDateTime.now());
        return fileRepository.save(f);
    }

    public FileEntity restoreFromTrash(Long fileId) {
        FileEntity f = getFileByIdSecure(fileId);
        f.setTrashed(false);
        f.setTrashedAt(null);
        return fileRepository.save(f);
    }

    public void deletePermanently(Long fileId) throws IOException {
        FileEntity f = getFileByIdSecure(fileId);
        Files.deleteIfExists(Paths.get(f.getPath()));
        fileRepository.delete(f);
    }

    public void emptyBin() throws IOException {
        List<FileEntity> trashed = fileRepository.findByUserIdAndTrashedTrue(currentUser().getId());
        for (FileEntity f : trashed) {
            Files.deleteIfExists(Paths.get(f.getPath()));
            fileRepository.delete(f);
        }
    }

    public List<FileEntity> getTrashedFiles() {
        return fileRepository.findByUserIdAndTrashedTrue(currentUser().getId());
    }

    // ── Spam ──────────────────────────────────────────────────────────────
    public FileEntity markAsSpam(Long fileId) {
        FileEntity f = getFileByIdSecure(fileId);
        f.setSpam(true);
        return fileRepository.save(f);
    }

    public FileEntity unmarkSpam(Long fileId) {
        FileEntity f = getFileByIdSecure(fileId);
        f.setSpam(false);
        return fileRepository.save(f);
    }

    public List<FileEntity> getSpamFiles() {
        return fileRepository.findByUserIdAndSpamTrue(currentUser().getId());
    }

    // ── Search ────────────────────────────────────────────────────────────
    public List<FileEntity> searchByName(String query) {
        return fileRepository
                .findByUserIdAndNameContainingIgnoreCaseAndTrashedFalseAndSpamFalse(currentUser().getId(), query);
    }

    // ── Storage ───────────────────────────────────────────────────────────
    public StorageInfoDTO getStorageInfo() {
        long used = fileRepository.sumStorageUsedByUser(currentUser().getId());
        return new StorageInfoDTO(used, storageLimitBytes);
    }

    // ── SECURE file fetch: ensures file belongs to logged-in user ─────────
    public FileEntity getFileByIdSecure(Long fileId) {
        Long uid = currentUser().getId();
        return fileRepository.findByIdAndUserId(fileId, uid)
                .orElseThrow(() -> new RuntimeException("File not found or access denied"));
    }
}
