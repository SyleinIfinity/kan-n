// Đặt tại: data/models/User.java
package com.kan_n.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {

    private String uid;
    private String username;
    private String displayName;
    private String email;
    private String avatarUrl;
    private String phone;
    private long createdAt;
    private boolean isActive;

    public User() {
        // Constructor trống
    }
    public User(String username, String displayName, String email, String avatarUrl, String phone) {
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.phone = phone; // Gán SDT
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
    }

    // --- Getters and Setters ---

    @Exclude
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("displayName", displayName);
        result.put("email", email);
        result.put("avatarUrl", avatarUrl);
        result.put("phone", phone); // ✨ 4. BỔ SUNG VÀO MAP
        result.put("createdAt", createdAt);
        result.put("isActive", isActive);
        return result;
    }
}