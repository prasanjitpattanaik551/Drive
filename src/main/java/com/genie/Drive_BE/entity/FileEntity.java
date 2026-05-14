package com.genie.Drive_BE.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String path;
    private Long size;

    // "file" or "folder"
    private String type;

    private Long parentFolderId;

    private LocalDateTime createdAt;

    // ── Recent: last time this file was opened ─────────────────────────────
    private LocalDateTime lastOpenedAt;

    // ── Starred ────────────────────────────────────────────────────────────
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean starred = false;

    // ── Bin (soft delete) ──────────────────────────────────────────────────
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean trashed = false;

    private LocalDateTime trashedAt;

    // ── Spam ───────────────────────────────────────────────────────────────
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean spam = false;

    // ── Owner (for "Shared with me" — owner's email) ───────────────────────
    private String ownerEmail;

    // ── Shared with (comma-separated emails, or use a join table for scale) ─
    @ElementCollection
    @CollectionTable(name = "file_shared_with", joinColumns = @JoinColumn(name = "file_id"))
    @Column(name = "shared_email")
    private List<String> sharedWith;

    // ─── Constructors ──────────────────────────────────────────────────────

    public FileEntity() {}

    // ─── Getters & Setters ─────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getParentFolderId() { return parentFolderId; }
    public void setParentFolderId(Long parentFolderId) { this.parentFolderId = parentFolderId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastOpenedAt() { return lastOpenedAt; }
    public void setLastOpenedAt(LocalDateTime lastOpenedAt) { this.lastOpenedAt = lastOpenedAt; }

    public boolean isStarred() { return starred; }
    public void setStarred(boolean starred) { this.starred = starred; }

    public boolean isTrashed() { return trashed; }
    public void setTrashed(boolean trashed) { this.trashed = trashed; }

    public LocalDateTime getTrashedAt() { return trashedAt; }
    public void setTrashedAt(LocalDateTime trashedAt) { this.trashedAt = trashedAt; }

    public boolean isSpam() { return spam; }
    public void setSpam(boolean spam) { this.spam = spam; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public List<String> getSharedWith() { return sharedWith; }
    public void setSharedWith(List<String> sharedWith) { this.sharedWith = sharedWith; }
}
