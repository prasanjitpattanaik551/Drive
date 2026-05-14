package com.genie.Drive_BE.services;

import com.genie.Drive_BE.dto.StorageInfoDTO;
import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.repo.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileServiceStorage {

    // 15 GB storage cap (changeable via application.properties)
    @Value("${storage.limit-bytes:16106127360}")
    private long storageLimitBytes;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final FileRepository fileRepository;

    public FileServiceStorage(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UPLOAD
    // ════════════════════════════════════════════════════════════════════════

    public String saveFile(MultipartFile file, Long parentFolderId) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = file.getOriginalFilename();
        Path   filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        FileEntity entity = new FileEntity();
        entity.setName(fileName);
        entity.setPath(filePath.toString());
        entity.setSize(file.getSize());
        entity.setType("file");
        entity.setParentFolderId(parentFolderId);
        entity.setCreatedAt(LocalDateTime.now());

        fileRepository.save(entity);
        return "File uploaded successfully!";
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MY DRIVE — files in a folder (root if parentFolderId is null)
    // ════════════════════════════════════════════════════════════════════════

    public List<FileEntity> getFilesInFolder(Long parentFolderId) {
        if (parentFolderId == null) {
            return fileRepository.findByParentFolderIdIsNullAndTrashedFalseAndSpamFalse();
        }
        return fileRepository.findByParentFolderIdAndTrashedFalseAndSpamFalse(parentFolderId);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RECENT — mark a file as opened, then fetch the 50 most recently opened
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Call this whenever a user opens / downloads a file.
     * Updates lastOpenedAt so it shows up in Recent.
     */
    public FileEntity markAsOpened(Long id) {
        FileEntity entity = getFileById(id);
        entity.setLastOpenedAt(LocalDateTime.now());
        return fileRepository.save(entity);
    }

    public List<FileEntity> getRecentFiles() {
        return fileRepository
                .findTop50ByLastOpenedAtIsNotNullAndTrashedFalseOrderByLastOpenedAtDesc();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  STARRED
    // ════════════════════════════════════════════════════════════════════════

    public FileEntity toggleStar(Long id) {
        FileEntity entity = getFileById(id);
        entity.setStarred(!entity.isStarred());
        return fileRepository.save(entity);
    }

    public List<FileEntity> getStarredFiles() {
        return fileRepository.findByStarredTrueAndTrashedFalseAndSpamFalse();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  BIN (soft-delete / restore / permanent delete)
    // ════════════════════════════════════════════════════════════════════════

    /** Move a file to the Bin (soft delete). */
    public FileEntity moveToTrash(Long id) {
        FileEntity entity = getFileById(id);
        entity.setTrashed(true);
        entity.setTrashedAt(LocalDateTime.now());
        return fileRepository.save(entity);
    }

    /** Restore a file from the Bin back to My Drive. */
    public FileEntity restoreFromTrash(Long id) {
        FileEntity entity = getFileById(id);
        entity.setTrashed(false);
        entity.setTrashedAt(null);
        return fileRepository.save(entity);
    }

    /** Permanently delete: remove from disk AND database. */
    public void deletePermanently(Long id) throws IOException {
        FileEntity entity = getFileById(id);
        Path path = Paths.get(entity.getPath());
        Files.deleteIfExists(path);
        fileRepository.deleteById(id);
    }

    /** Empty the entire bin. */
    public void emptyBin() throws IOException {
        List<FileEntity> trashed = fileRepository.findByTrashedTrue();
        for (FileEntity entity : trashed) {
            Path path = Paths.get(entity.getPath());
            Files.deleteIfExists(path);
            fileRepository.delete(entity);
        }
    }

    public List<FileEntity> getTrashedFiles() {
        return fileRepository.findByTrashedTrue();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SPAM
    // ════════════════════════════════════════════════════════════════════════

    public FileEntity markAsSpam(Long id) {
        FileEntity entity = getFileById(id);
        entity.setSpam(true);
        return fileRepository.save(entity);
    }

    public FileEntity unmarkSpam(Long id) {
        FileEntity entity = getFileById(id);
        entity.setSpam(false);
        return fileRepository.save(entity);
    }

    public List<FileEntity> getSpamFiles() {
        return fileRepository.findBySpamTrue();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SHARED WITH ME
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Share a file with one or more email addresses.
     * The caller passes the owner's email so the recipient can see "shared by".
     */
    public FileEntity shareFile(Long id, String ownerEmail, List<String> recipientEmails) {
        FileEntity entity = getFileById(id);

        // Set owner if not already set
        if (entity.getOwnerEmail() == null) {
            entity.setOwnerEmail(ownerEmail);
        }

        // Merge new emails into existing list (avoid duplicates)
        List<String> existing = entity.getSharedWith() != null
                ? new ArrayList<>(entity.getSharedWith())
                : new ArrayList<>();
        for (String email : recipientEmails) {
            if (!existing.contains(email)) {
                existing.add(email);
            }
        }
        entity.setSharedWith(existing);
        return fileRepository.save(entity);
    }

    /**
     * Returns all files shared with a specific email address.
     * Frontend passes the logged-in user's email.
     */
    public List<FileEntity> getFilesSharedWithMe(String email) {
        return fileRepository.findFilesSharedWithEmail(email);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SEARCH (header search bar)
    // ════════════════════════════════════════════════════════════════════════

    public List<FileEntity> searchByName(String query) {
        return fileRepository
                .findByNameContainingIgnoreCaseAndTrashedFalseAndSpamFalse(query);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  STORAGE INFO (sidebar storage bar)
    // ════════════════════════════════════════════════════════════════════════

    public StorageInfoDTO getStorageInfo() {
        long used = fileRepository.sumStorageUsed();
        return new StorageInfoDTO(used, storageLimitBytes);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  COMMON HELPERS
    // ════════════════════════════════════════════════════════════════════════

    public FileEntity getFileById(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found: " + id));
    }

    /** Legacy hard-delete used by the old DELETE /delete/{id} endpoint. */
    public void deleteById(Long id) {
        fileRepository.deleteById(id);
    }
}
