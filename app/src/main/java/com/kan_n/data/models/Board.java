package com.kan_n.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Board {

    private String uid;
    private String workspaceId; // Workspace UID
    private String name;
    private String description;
    private Boolean visibility;
    private String createdBy; // User UID
    private long createdAt;
    private boolean isArchived;

    public Board() {
        // Constructor trống
    }

    public Board(String workspaceId, String name, String description, Boolean visibility, String createdBy) {
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.visibility = visibility;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
        this.isArchived = false;
    }

    // --- Getters and Setters ---

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

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
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
        return result;
    }
}