package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.dto.StorageInfoDTO;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Storage bar (bottom of sidebar).
 *
 *  GET /api/storage   → returns { usedBytes, totalBytes, usedPercent }
 */
@RestController
@RequestMapping("/api/storage")
@CrossOrigin(origins = "http://localhost:3000")
public class StorageController {

    private final FileServiceStorage fileServiceStorage;

    public StorageController(FileServiceStorage fileServiceStorage) {
        this.fileServiceStorage = fileServiceStorage;
    }

    @GetMapping
    public ResponseEntity<StorageInfoDTO> getStorageInfo() {
        return ResponseEntity.ok(fileServiceStorage.getStorageInfo());
    }
}
