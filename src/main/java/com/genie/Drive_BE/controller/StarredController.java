package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/starred")

public class StarredController {

    private final FileServiceStorage svc;

    public StarredController(FileServiceStorage svc) {
        this.svc = svc;
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> list() {
        return ResponseEntity.ok(svc.getStarredFiles());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FileEntity> toggle(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(svc.toggleStar(id));
        } catch (Exception e) {
            return ResponseEntity.status(403).build();
        }
    }
}