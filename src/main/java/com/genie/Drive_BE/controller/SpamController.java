package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Spam section (⚠ in the sidebar).
 *
 *  GET    /api/spam            → list all spam files
 *  PATCH  /api/spam/{id}       → mark a file as spam
 *  PATCH  /api/spam/{id}/clear → remove a file from spam
 */
@RestController
@RequestMapping("/api/spam")
@CrossOrigin(origins = "http://localhost:3000")
public class SpamController {

    private final FileServiceStorage fileServiceStorage;

    public SpamController(FileServiceStorage fileServiceStorage) {
        this.fileServiceStorage = fileServiceStorage;
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> getSpam() {
        return ResponseEntity.ok(fileServiceStorage.getSpamFiles());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FileEntity> markSpam(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(fileServiceStorage.markAsSpam(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    @PatchMapping("/{id}/clear")
    public ResponseEntity<FileEntity> clearSpam(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(fileServiceStorage.unmarkSpam(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
}
