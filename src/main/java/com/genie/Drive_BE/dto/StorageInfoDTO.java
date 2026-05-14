package com.genie.Drive_BE.dto;

/**
 * Returned by GET /api/storage
 * Powers the "X GB of 15 GB used" storage bar in the sidebar.
 */
public class StorageInfoDTO {

    private long usedBytes;
    private long totalBytes;
    private double usedPercent;

    public StorageInfoDTO(long usedBytes, long totalBytes) {
        this.usedBytes   = usedBytes;
        this.totalBytes  = totalBytes;
        this.usedPercent = totalBytes > 0
                ? Math.round((usedBytes * 100.0 / totalBytes) * 100.0) / 100.0
                : 0.0;
    }

    public long getUsedBytes()      { return usedBytes; }
    public long getTotalBytes()     { return totalBytes; }
    public double getUsedPercent()  { return usedPercent; }
}
