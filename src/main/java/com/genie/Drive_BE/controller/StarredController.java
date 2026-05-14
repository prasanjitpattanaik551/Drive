package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Starred section (☆ in the sidebar).
 *
 *  GET   /api/starred          → list all starred files
 *  PATCH /api/starred/{id}     → toggle star on/off
 */
@RestController
@RequestMapping("/api/starred")
@CrossOrigin(origins = "http://localhost:3000")
public class StarredController {

    private final FileServiceStorage fileServiceStorage;

    public StarredController(FileServiceStorage fileServiceStorage) {
        this.fileServiceStorage = fileServiceStorage;
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> getStarred() {
        return ResponseEntity.ok(fileServiceStorage.getStarredFiles());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FileEntity> toggleStar(@PathVariable Long id) {
        try {
            FileEntity updated = fileServiceStorage.toggleStar(id);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }
}
