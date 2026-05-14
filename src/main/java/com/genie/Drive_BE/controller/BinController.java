package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Bin / Trash section (🗑 in the sidebar).
 *
 *  GET    /api/bin                  → list all trashed files
 *  PATCH  /api/bin/{id}/restore     → restore a file back to My Drive
 *  DELETE /api/bin/{id}             → permanently delete a single file
 *  DELETE /api/bin                  → empty the entire bin
 */
@RestController
@RequestMapping("/api/bin")
@CrossOrigin(origins = "http://localhost:3000")
public class BinController {

    private final FileServiceStorage fileServiceStorage;

    public BinController(FileServiceStorage fileServiceStorage) {
        this.fileServiceStorage = fileServiceStorage;
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> getBin() {
        return ResponseEntity.ok(fileServiceStorage.getTrashedFiles());
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<FileEntity> restore(@PathVariable Long id) {
        try {
            FileEntity restored = fileServiceStorage.restoreFromTrash(id);
            return ResponseEntity.ok(restored);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePermanently(@PathVariable Long id) {
        try {
            fileServiceStorage.deletePermanently(id);
            return ResponseEntity.ok("File permanently deleted.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete: " + e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<String> emptyBin() {
        try {
            fileServiceStorage.emptyBin();
            return ResponseEntity.ok("Bin emptied.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to empty bin: " + e.getMessage());
        }
    }
}
