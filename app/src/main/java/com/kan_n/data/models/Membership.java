package com.kan_n.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Membership {

    private String uid;
    private String boardId;
    private String userId;
    private String role;
    private long joinedAt;
    private String permission;

    public Membership() {

    }

    public Membership(String boardId, String userId, String role) {
        this.boardId = boardId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = System.currentTimeMillis();
    }
    public Membership(String boardId, String userId, String role, String permission) {
        this.boardId = boardId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = System.currentTimeMillis();
        this.permission = permission;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(long joinedAt) {
        this.joinedAt = joinedAt;
    }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("boardId", boardId);
        result.put("userId", userId);
        result.put("role", role);
        result.put("joinedAt", joinedAt);
        result.put("permission", permission);
        return result;
    }
}