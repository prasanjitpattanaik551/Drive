package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.dto.ShareRequestDTO;
import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Shared-with-me section (👥 in the sidebar).
 *
 *  POST /api/shared/{id}               → share a file with one or more email addresses
 *      Body: { "emails": ["a@x.com", "b@x.com"] }
 *      Header: X-Owner-Email: owner@example.com   (who is sharing)
 *
 *  GET  /api/shared/with-me            → files shared with the logged-in user
 *      Header: X-User-Email: me@example.com
 */
@RestController
@RequestMapping("/api/shared")
@CrossOrigin(origins = "http://localhost:3000")
public class SharedController {

    private final FileServiceStorage fileServiceStorage;

    public SharedController(FileServiceStorage fileServiceStorage) {
        this.fileServiceStorage = fileServiceStorage;
    }

    /**
     * Share a file.
     * The owner's email comes from a request header (replace with JWT claim
     * once you add authentication).
     */
    @PostMapping("/{id}")
    public ResponseEntity<FileEntity> shareFile(
            @PathVariable Long id,
            @RequestHeader("X-Owner-Email") String ownerEmail,
            @RequestBody ShareRequestDTO request) {
        try {
            FileEntity updated = fileServiceStorage.shareFile(id, ownerEmail, request.getEmails());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Get all files shared with the calling user.
     * The user's email comes from a request header (replace with JWT once ready).
     */
    @GetMapping("/with-me")
    public ResponseEntity<List<FileEntity>> getSharedWithMe(
            @RequestHeader("X-User-Email") String userEmail) {
        return ResponseEntity.ok(fileServiceStorage.getFilesSharedWithMe(userEmail));
    }
}
