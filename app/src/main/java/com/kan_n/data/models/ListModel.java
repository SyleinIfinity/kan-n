package com.kan_n.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

// Đặt tên là ListModel để tránh xung đột với java.util.List
@IgnoreExtraProperties
public class ListModel {

    private String uid;
    private String boardId;
    private String title;
    private double position; // Dùng double để dễ dàng sắp xếp lại
    private boolean isClosed;
    private long createdAt;

    public ListModel() {
        // Constructor trống
    }

    public ListModel(String boardId, String title, double position) {
        this.boardId = boardId;
        this.title = title;
        this.position = position;
        this.isClosed = false;
        this.createdAt = System.currentTimeMillis();
    }

    // --- Getters and Setters ---

    @Exclude
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("boardId", boardId);
        result.put("title", title);
        result.put("position", position);
        result.put("isClosed", isClosed);
        result.put("createdAt", createdAt);
        return result;
    }
}