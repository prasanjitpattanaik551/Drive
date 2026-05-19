package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/storage")
@CrossOrigin(origins = "http://localhost:3000")
public class StorageController {

    private final FileServiceStorage svc;

    public StorageController(FileServiceStorage svc) {
        this.svc = svc;
    }

    @GetMapping
    public ResponseEntity<?> info() {
        return ResponseEntity.ok(svc.getStorageInfo());
    }
}