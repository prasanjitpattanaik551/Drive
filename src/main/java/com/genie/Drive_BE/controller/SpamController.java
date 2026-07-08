package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/spam")

public class SpamController {

    private final FileServiceStorage svc;

    public SpamController(FileServiceStorage svc) {
        this.svc = svc;
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> list() {
        return ResponseEntity.ok(svc.getSpamFiles());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FileEntity> mark(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(svc.markAsSpam(id));
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
    }

    @PatchMapping("/{id}/clear")
    public ResponseEntity<FileEntity> clear(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(svc.unmarkSpam(id));
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
    }
}