package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/search")

public class SearchController {

    private final FileServiceStorage svc;

    public SearchController(FileServiceStorage svc) {
        this.svc = svc;
    }

    @GetMapping
    public ResponseEntity<List<FileEntity>> search(@RequestParam("q") String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(svc.searchByName(q.trim()));
    }
}