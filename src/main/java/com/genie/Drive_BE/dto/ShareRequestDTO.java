package com.genie.Drive_BE.dto;

import java.util.List;

/**
 * Request body for POST /api/files/{id}/share
 * {
 *   "emails": ["alice@example.com", "bob@example.com"]
 * }
 */
public class ShareRequestDTO {

    private List<String> emails;

    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }
}
