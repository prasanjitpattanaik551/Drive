package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recent")
@CrossOrigin(origins = "http://localhost:3000")
public class RecentController {

    private final FileServiceStorage svc;

    public RecentController(FileServiceStorage svc) {
        this.svc = svc;
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> list() {
        return ResponseEntity.ok(svc.getRecentFiles());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FileEntity> markOpened(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(svc.markAsOpened(id));
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
    }
}