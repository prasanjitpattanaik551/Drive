package com.genie.Drive_BE.repo;

import com.genie.Drive_BE.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    // ── My Drive: root-level files that are not trashed or spammed ──────────
    List<FileEntity> findByParentFolderIdIsNullAndTrashedFalseAndSpamFalse();

    // ── Files inside a specific folder ─────────────────────────────────────
    List<FileEntity> findByParentFolderIdAndTrashedFalseAndSpamFalse(Long parentFolderId);

    // ── Starred ─────────────────────────────────────────────────────────────
    List<FileEntity> findByStarredTrueAndTrashedFalseAndSpamFalse();

    // ── Bin (trashed) ────────────────────────────────────────────────────────
    List<FileEntity> findByTrashedTrue();

    // ── Spam ─────────────────────────────────────────────────────────────────
    List<FileEntity> findBySpamTrue();

    // ── Recent: last 50 opened files ordered by lastOpenedAt ─────────────────
    List<FileEntity> findTop50ByLastOpenedAtIsNotNullAndTrashedFalseOrderByLastOpenedAtDesc();

    // ── Shared with me: files where the given email is in sharedWith list ────
    @Query("SELECT f FROM FileEntity f JOIN f.sharedWith s WHERE s = :email AND f.trashed = false AND f.spam = false")
    List<FileEntity> findFilesSharedWithEmail(@Param("email") String email);

    // ── Search by name (case-insensitive, excludes trash & spam) ─────────────
    List<FileEntity> findByNameContainingIgnoreCaseAndTrashedFalseAndSpamFalse(String name);

    // ── Storage: sum of all non-trashed file sizes ────────────────────────────
    @Query("SELECT COALESCE(SUM(f.size), 0) FROM FileEntity f WHERE f.trashed = false AND f.type = 'file'")
    Long sumStorageUsed();
}
