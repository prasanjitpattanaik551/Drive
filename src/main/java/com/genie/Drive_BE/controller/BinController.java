package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bin")
@CrossOrigin(origins = "http://localhost:3000")
public class BinController {

    private final FileServiceStorage svc;

    public BinController(FileServiceStorage svc) {
        this.svc = svc;
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> list() {
        return ResponseEntity.ok(svc.getTrashedFiles());
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<FileEntity> restore(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(svc.restoreFromTrash(id));
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteForever(@PathVariable Long id) {
        try {
            svc.deletePermanently(id);
            return ResponseEntity.ok("Permanently deleted.");
        } catch (Exception e) {
            return ResponseEntity.status(403).body("Access denied");
        }
    }

    @DeleteMapping
    public ResponseEntity<String> emptyBin() {
        try {
            svc.emptyBin();
            return ResponseEntity.ok("Bin emptied.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}