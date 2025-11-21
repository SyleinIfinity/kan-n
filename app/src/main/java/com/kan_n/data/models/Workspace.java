// Đặt tại: app/src/main/java/com/kan_n/data/models/Workspace.java

package com.kan_n.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List; // ✨ 1. BẠN CẦN IMPORT NÀY
import java.util.Map;

@IgnoreExtraProperties
public class Workspace {
    private String uid;
    private String name;
    private String description;
    private long createdAt;
    private String createdBy; // (Đã bổ sung ở bước trước)
    private List<Board> boards;

    public Workspace() {
        // Constructor trống
    }

    public Workspace(String name, String description, String createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Dùng @Exclude để Firebase không cố gắng đọc/lưu trường này.
     * Nó chỉ được dùng ở phía client (ứng dụng) sau khi gộp dữ liệu.
     */
    @Exclude
    public List<Board> getBoards() {
        return boards;
    }

    /**
     * Đây là phương thức bạn đang thiếu, dùng để gán danh sách Bảng
     * vào Workspace trong file BoardRepositoryImpl.
     */
    @Exclude
    public void setBoards(List<Board> boards) {
        this.boards = boards;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("description", description);
        result.put("createdAt", createdAt);
        result.put("createdBy", createdBy);
        // 'boards' sẽ không được thêm vào map
        return result;
    }
}