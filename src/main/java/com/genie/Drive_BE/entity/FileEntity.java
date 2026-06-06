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

    // ── CRITICAL: every file belongs to one user ──────────────────────────
    @Column(nullable = false)
    private Long userId;

    private String name;
    private String path;
    private Long   size;
    private String type;           // "file" | "folder"
    private Long   parentFolderId;

    private LocalDateTime createdAt;
    private LocalDateTime lastOpenedAt;

    private boolean starred = false;
    private boolean trashed = false;
    private LocalDateTime trashedAt;
    private boolean spam    = false;

    // ── Getters & Setters ─────────────────────────────────────────────────
    public Long   getId()
    {
        return id;
    }
    public void   setId(Long id)            { this.id = id; }

    public Long   getUserId()               { return userId; }
    public void   setUserId(Long userId)    { this.userId = userId; }

    public String getName()                 { return name; }
    public void   setName(String n)         { this.name = n; }

    public String getPath()                 { return path; }
    public void   setPath(String p)         { this.path = p; }

    public Long   getSize()                 { return size; }
    public void   setSize(Long s)           { this.size = s; }

    public String getType()                 { return type; }
    public void   setType(String t)         { this.type = t; }

    public Long   getParentFolderId()       { return parentFolderId; }
    public void   setParentFolderId(Long p) { this.parentFolderId = p; }

    public LocalDateTime getCreatedAt()     { return createdAt; }
    public void setCreatedAt(LocalDateTime t){ this.createdAt = t; }

    public LocalDateTime getLastOpenedAt()  { return lastOpenedAt; }
    public void setLastOpenedAt(LocalDateTime t){ this.lastOpenedAt = t; }

    public boolean isStarred()              { return starred; }
    public void    setStarred(boolean v)    { this.starred = v; }

    public boolean isTrashed()              { return trashed; }
    public void    setTrashed(boolean v)    { this.trashed = v; }

    public LocalDateTime getTrashedAt()     { return trashedAt; }
    public void setTrashedAt(LocalDateTime t){ this.trashedAt = t; }

    public boolean isSpam()                 { return spam; }
    public void    setSpam(boolean v)       { this.spam = v; }
}
