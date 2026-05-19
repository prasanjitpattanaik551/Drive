package com.genie.Drive_BE.repo;

import com.genie.Drive_BE.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    // ── My Drive root (no parent folder) ─────────────────────────────────
    List<FileEntity> findByUserIdAndParentFolderIdIsNullAndTrashedFalseAndSpamFalse(Long userId);

    // ── Files inside a folder ─────────────────────────────────────────────
    List<FileEntity> findByUserIdAndParentFolderIdAndTrashedFalseAndSpamFalse(Long userId, Long parentFolderId);

    // ── Starred ───────────────────────────────────────────────────────────
    List<FileEntity> findByUserIdAndStarredTrueAndTrashedFalseAndSpamFalse(Long userId);

    // ── Bin ───────────────────────────────────────────────────────────────
    List<FileEntity> findByUserIdAndTrashedTrue(Long userId);

    // ── Spam ──────────────────────────────────────────────────────────────
    List<FileEntity> findByUserIdAndSpamTrue(Long userId);

    // ── Recent (last 50 opened) ───────────────────────────────────────────
    List<FileEntity> findTop50ByUserIdAndLastOpenedAtIsNotNullAndTrashedFalseOrderByLastOpenedAtDesc(Long userId);

    // ── Search by name ────────────────────────────────────────────────────
    List<FileEntity> findByUserIdAndNameContainingIgnoreCaseAndTrashedFalseAndSpamFalse(Long userId, String name);

    // ── Storage: sum file sizes for this user ─────────────────────────────
    @Query("SELECT COALESCE(SUM(f.size),0) FROM FileEntity f WHERE f.userId = :userId AND f.trashed = false AND f.type = 'file'")
    Long sumStorageUsedByUser(@Param("userId") Long userId);

    // ── Secure single-file fetch (must belong to user) ────────────────────
    Optional<FileEntity> findByIdAndUserId(Long id, Long userId);
}
