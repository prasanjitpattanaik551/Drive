package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Recent section (🕐 in the sidebar).
 *
 *  GET   /api/recent          → list the 50 most recently opened files
 *  PATCH /api/recent/{id}     → manually mark a file as opened (e.g. preview)
 *
 *  Note: downloading via GET /api/files/download/{id} also updates lastOpenedAt.
 */
@RestController
@RequestMapping("/api/recent")
@CrossOrigin(origins = "http://localhost:3000")
public class RecentController {

    private final FileServiceStorage fileServiceStorage;

    public RecentController(FileServiceStorage fileServiceStorage) {
        this.fileServiceStorage = fileServiceStorage;
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> getRecent() {
        return ResponseEntity.ok(fileServiceStorage.getRecentFiles());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FileEntity> markOpened(@PathVariable Long id) {
        try {
            FileEntity updated = fileServiceStorage.markAsOpened(id);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
}
