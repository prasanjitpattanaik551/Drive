package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Handles the original file CRUD endpoints.
 * All new feature endpoints live in their own dedicated controllers below.
 */
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:3000")
public class FileController {

    private final FileServiceStorage fileServiceStorage;

    public FileController(FileServiceStorage fileServiceStorage) {
        this.fileServiceStorage = fileServiceStorage;
    }

    // ── Upload ───────────────────────────────────────────────────────────────

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentFolderId", required = false) Long parentFolderId) {
        try {
            String response = fileServiceStorage.saveFile(file, parentFolderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }

    // ── Download (also marks the file as "recently opened") ─────────────────

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        try {
            FileEntity fileEntity = fileServiceStorage.markAsOpened(id); // ← updates lastOpenedAt
            Path path = Paths.get(fileEntity.getPath());
            Resource resource = new UrlResource(path.toUri());
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileEntity.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // ── List files in a folder (My Drive) ────────────────────────────────────

    @GetMapping("/list")
    public ResponseEntity<List<FileEntity>> listFiles(
            @RequestParam(value = "parentFolderId", required = false) Long parentFolderId) {
        return ResponseEntity.ok(fileServiceStorage.getFilesInFolder(parentFolderId));
    }

    // ── Soft-delete → Bin  (replaces the old hard-delete) ───────────────────

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        try {
            fileServiceStorage.moveToTrash(id);
            return ResponseEntity.ok("File moved to bin.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to move file to bin: " + e.getMessage());
        }
    }
}
