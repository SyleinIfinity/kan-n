package com.kan_n.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {

    private String uid; // Sẽ là key của node
    private String username;
    private String password_hash;
    private String displayName;
    private String email;
    private String avatarUrl;
    private long createdAt;
    private boolean isActive;

    public User() {
        // Constructor trống bắt buộc cho Firebase
    }

    public User(String username, String password_hash, String displayName, String email) {
        this.username = username;
        this.password_hash = password_hash;
        this.displayName = displayName;
        this.email = email;
        this.avatarUrl = null; // Theo yêu cầu
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
    }

    // --- Getters and Setters ---

    @Exclude
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("password_hash", password_hash);
        result.put("displayName", displayName);
        result.put("email", email);
        result.put("avatarUrl", avatarUrl);
        result.put("createdAt", createdAt);
        result.put("isActive", isActive);
        return result;
    }
}