package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Global search (🔍 in the header).
 *
 *  GET /api/search?q=report   → returns all non-trashed, non-spam files
 *                               whose name contains "report" (case-insensitive)
 */
@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:3000")
public class SearchController {

    private final FileServiceStorage fileServiceStorage;

    public SearchController(FileServiceStorage fileServiceStorage) {
        this.fileServiceStorage = fileServiceStorage;
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> search(@RequestParam("q") String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(fileServiceStorage.searchByName(query.trim()));
    }
}
