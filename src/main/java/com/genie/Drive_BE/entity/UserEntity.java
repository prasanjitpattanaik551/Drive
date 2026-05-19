package com.genie.Drive_BE.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String avatarColor = "#1A73E8";
    private String avatarUrl;
    private String phone;
    private String bio;

    private String  theme    = "light";
    private boolean emailNotifications  = true;
    private boolean uploadNotifications = true;
    private boolean shareNotifications  = true;
    private String  aiProvider = "gemini";

    @Column(columnDefinition = "TEXT")
    private String aiApiKey;

    // OTP login
    private String        otpCode;
    private LocalDateTime otpExpiresAt;
    private boolean       verified = false;

    // Token-based auth — stored in DB, sent by frontend in X-Auth-Token header
    @Column(unique = true)
    private String sessionToken;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist  public void onCreate() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate   public void onUpdate() { updatedAt = LocalDateTime.now(); }

    public Long   getId()                   { return id; }
    public void   setId(Long id)            { this.id = id; }
    public String getName()                 { return name; }
    public void   setName(String n)         { this.name = n; }
    public String getEmail()                { return email; }
    public void   setEmail(String e)        { this.email = e; }
    public String getAvatarColor()          { return avatarColor; }
    public void   setAvatarColor(String c)  { this.avatarColor = c; }
    public String getAvatarUrl()            { return avatarUrl; }
    public void   setAvatarUrl(String u)    { this.avatarUrl = u; }
    public String getPhone()                { return phone; }
    public void   setPhone(String p)        { this.phone = p; }
    public String getBio()                  { return bio; }
    public void   setBio(String b)          { this.bio = b; }
    public String getTheme()                { return theme; }
    public void   setTheme(String t)        { this.theme = t; }
    public boolean isEmailNotifications()            { return emailNotifications; }
    public void    setEmailNotifications(boolean v)  { this.emailNotifications = v; }
    public boolean isUploadNotifications()           { return uploadNotifications; }
    public void    setUploadNotifications(boolean v) { this.uploadNotifications = v; }
    public boolean isShareNotifications()            { return shareNotifications; }
    public void    setShareNotifications(boolean v)  { this.shareNotifications = v; }
    public String  getAiProvider()                   { return aiProvider; }
    public void    setAiProvider(String p)           { this.aiProvider = p; }
    public String  getAiApiKey()                     { return aiApiKey; }
    public void    setAiApiKey(String k)             { this.aiApiKey = k; }
    public String  getOtpCode()                      { return otpCode; }
    public void    setOtpCode(String o)              { this.otpCode = o; }
    public LocalDateTime getOtpExpiresAt()           { return otpExpiresAt; }
    public void    setOtpExpiresAt(LocalDateTime t)  { this.otpExpiresAt = t; }
    public boolean isVerified()                      { return verified; }
    public void    setVerified(boolean v)            { this.verified = v; }
    public String  getSessionToken()                 { return sessionToken; }
    public void    setSessionToken(String t)         { this.sessionToken = t; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
}