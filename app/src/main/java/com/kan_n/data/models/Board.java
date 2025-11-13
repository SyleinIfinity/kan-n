// Đặt tại: app/src/main/java/com/kan_n/data/models/Board.java

package com.kan_n.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Board {

    private String uid;
    private String workspaceId;
    private String name;
    private String description;
    private String visibility;
    private String createdBy;
    private long createdAt;
    private boolean isArchived;

    // ✨ 1. BỔ SUNG TRƯỜNG MỚI
    private Background background;

    public Board() {
        // Constructor trống
    }

    // ✨ 2. CẬP NHẬT CONSTRUCTOR (thêm background)
    public Board(String workspaceId, String name, String description, String visibility, String createdBy, Background background) {
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.visibility = visibility;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
        this.isArchived = false;
        this.background = background; // <--- Cập nhật
    }

    // --- Getters and Setters (Giữ nguyên các getter/setter cũ) ---

    @Exclude
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    // ✨ 3. BỔ SUNG GETTER/SETTER CHO BACKGROUND
    public Background getBackground() {
        return background;
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("workspaceId", workspaceId);
        result.put("name", name);
        result.put("description", description);
        result.put("visibility", visibility);
        result.put("createdBy", createdBy);
        result.put("createdAt", createdAt);
        result.put("isArchived", isArchived);
        result.put("background", background); // ✨ 4. BỔ SUNG VÀO HÀM TOMAP
        return result;
    }
}