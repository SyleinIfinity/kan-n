package com.kan_n.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Card {

    private String uid;
    private String listId;
    private String title;
    private String description;
    private double position;
    private long dueDate;
    private String createdBy;
    private long createdAt;
    private boolean archived;
    private Map<String, Boolean> tagIds = new HashMap<>(); // Ánh xạ từ CardTags

    public Card() {
        // Constructor trống
    }

    public Card(String listId, String title, double position, String createdBy) {
        this.listId = listId;
        this.title = title;
        this.position = position;
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
        this.description = "";
        this.dueDate = 0;
        this.archived = false;
    }

    // --- Getters and Setters ---

    @Exclude
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
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
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Map<String, Boolean> getTagIds() {
        return tagIds;
    }

    public void setTagIds(Map<String, Boolean> tagIds) {
        this.tagIds = tagIds;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("listId", listId);
        result.put("title", title);
        result.put("description", description);
        result.put("position", position);
        result.put("dueDate", dueDate);
        result.put("createdBy", createdBy);
        result.put("createdAt", createdAt);
        result.put("archived", archived);
        result.put("tagIds", tagIds);
        return result;
    }
}