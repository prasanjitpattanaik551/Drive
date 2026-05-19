package com.genie.Drive_BE.controller;

import com.genie.Drive_BE.entity.FileEntity;
import com.genie.Drive_BE.services.FileServiceStorage;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:3000")
public class FileController {

    private final FileServiceStorage fileServiceStorage;

    public FileController(FileServiceStorage fileServiceStorage) {
        this.fileServiceStorage = fileServiceStorage;
    }

    // ── Upload ────────────────────────────────────────────────────────────
    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentFolderId", required = false) Long parentFolderId) {
        try {
            return ResponseEntity.ok(fileServiceStorage.saveFile(file, parentFolderId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    // ── Download / Preview — correct Content-Type set here ────────────────
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        try {
            FileEntity f  = fileServiceStorage.markAsOpened(id);
            Path path     = Paths.get(f.getPath());
            Resource resource = new UrlResource(path.toUri());

            // ── Detect correct MIME type ──────────────────────────────────
            String mimeType = getMimeType(f.getName());
            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(mimeType);
            } catch (Exception e) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            return ResponseEntity.ok()
                    // Content-Type tells browser HOW to handle the file
                    .contentType(mediaType)
                    // inline = browser tries to render it; attachment = forces download
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            isPreviewable(f.getName())
                                    ? "inline; filename=\"" + f.getName() + "\""
                                    : "attachment; filename=\"" + f.getName() + "\"")
                    // Required for browser to read the blob in JavaScript
                    .header("Access-Control-Expose-Headers", "Content-Disposition")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // ── List files ────────────────────────────────────────────────────────
    @GetMapping("/list")
    public ResponseEntity<List<FileEntity>> list(
            @RequestParam(value = "parentFolderId", required = false) Long parentFolderId) {
        return ResponseEntity.ok(fileServiceStorage.getFilesInFolder(parentFolderId));
    }

    // ── Soft-delete (move to bin) ─────────────────────────────────────────
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        try {
            fileServiceStorage.moveToTrash(id);
            return ResponseEntity.ok("Moved to bin.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed: " + e.getMessage());
        }
    }

    // ── MIME type map ─────────────────────────────────────────────────────
    private String getMimeType(String filename) {
        String ext = "";
        int dot = filename.lastIndexOf('.');
        if (dot >= 0) ext = filename.substring(dot + 1).toLowerCase();

        return switch (ext) {
            // Documents
            case "pdf"  -> "application/pdf";
            case "doc"  -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls"  -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt"  -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";

            // Images
            case "jpg", "jpeg" -> "image/jpeg";
            case "png"         -> "image/png";
            case "gif"         -> "image/gif";
            case "webp"        -> "image/webp";
            case "bmp"         -> "image/bmp";
            case "svg"         -> "image/svg+xml";
            case "ico"         -> "image/x-icon";

            // Video
            case "mp4"  -> "video/mp4";
            case "webm" -> "video/webm";
            case "mov"  -> "video/quicktime";
            case "avi"  -> "video/x-msvideo";
            case "mkv"  -> "video/x-matroska";

            // Audio
            case "mp3"  -> "audio/mpeg";
            case "wav"  -> "audio/wav";
            case "ogg"  -> "audio/ogg";
            case "aac"  -> "audio/aac";
            case "flac" -> "audio/flac";
            case "m4a"  -> "audio/mp4";

            // Text / Code
            case "txt"  -> "text/plain";
            case "html", "htm" -> "text/html";
            case "css"  -> "text/css";
            case "csv"  -> "text/csv";
            case "xml"  -> "text/xml";
            case "json" -> "application/json";
            case "js"   -> "application/javascript";
            case "ts"   -> "application/typescript";
            case "md"   -> "text/markdown";
            case "yaml", "yml" -> "text/yaml";
            case "sh"   -> "text/x-sh";
            case "py"   -> "text/x-python";
            case "java" -> "text/x-java-source";
            case "log"  -> "text/plain";

            // Archives
            case "zip"  -> "application/zip";
            case "rar"  -> "application/vnd.rar";
            case "tar"  -> "application/x-tar";
            case "gz"   -> "application/gzip";
            case "7z"   -> "application/x-7z-compressed";

            // Default
            default -> "application/octet-stream";
        };
    }

    // Files that browser can render inline (no forced download)
    private boolean isPreviewable(String filename) {
        String ext = "";
        int dot = filename.lastIndexOf('.');
        if (dot >= 0) ext = filename.substring(dot + 1).toLowerCase();
        return switch (ext) {
            case "pdf","jpg","jpeg","png","gif","webp","bmp","svg",
                 "mp4","webm","mp3","wav","ogg","aac",
                 "txt","md","csv","json","js","ts","py","java",
                 "html","css","xml","yaml","yml","sh","log","jsx","tsx" -> true;
            default -> false;
        };
    }
}
