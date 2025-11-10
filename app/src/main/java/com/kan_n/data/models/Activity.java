package com.kan_n.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Activity {

    private String uid;
    private String boardId;
    private String cardId;
    private String userId;
    private String actionType;
    private Map<String, Object> actionData; // Dữ liệu JSON linh hoạt
    private long createdAt;

    public Activity() {
        // Constructor trống
    }

    public Activity(String boardId, String cardId, String userId, String actionType, Map<String, Object> actionData) {
        this.boardId = boardId;
        this.cardId = cardId;
        this.userId = userId;
        this.actionType = actionType;
        this.actionData = actionData;
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

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Map<String, Object> getActionData() {
        return actionData;
    }

    public void setActionData(Map<String, Object> actionData) {
        this.actionData = actionData;
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
        result.put("cardId", cardId);
        result.put("userId", userId);
        result.put("actionType", actionType);
        result.put("actionData", actionData);
        result.put("createdAt", createdAt);
        return result;
    }
}
