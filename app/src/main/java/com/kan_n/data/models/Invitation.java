package com.kan_n.data.models;

import com.google.firebase.database.Exclude;
import java.util.HashMap;
import java.util.Map;

public class Invitation {
    private String uid;
    private String boardId;
    private String senderId;
    private String receiverId;
    private String receiverEmail;
    private String role;
    private String status; // "pending", "accepted", "declined"
    private long timestamp;

    // Thêm trường boardName và senderName để hiển thị cho người nhận dễ hiểu
    private String boardName;
    private String senderName;

    public Invitation() {
        // Constructor rỗng cho Firebase
    }

    public Invitation(String boardId, String boardName, String senderId, String senderName, String receiverId, String receiverEmail, String role) {
        this.boardId = boardId;
        this.boardName = boardName;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverEmail = receiverEmail;
        this.role = role;
        this.status = "pending";
        this.timestamp = System.currentTimeMillis();
    }

    // --- Getters and Setters ---
    @Exclude
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getBoardId() { return boardId; }
    public void setBoardId(String boardId) { this.boardId = boardId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverEmail() { return receiverEmail; }
    public void setReceiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getBoardName() { return boardName; }
    public void setBoardName(String boardName) { this.boardName = boardName; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("boardId", boardId);
        result.put("boardName", boardName);
        result.put("senderId", senderId);
        result.put("senderName", senderName);
        result.put("receiverId", receiverId);
        result.put("receiverEmail", receiverEmail);
        result.put("role", role);
        result.put("status", status);
        result.put("timestamp", timestamp);
        return result;
    }
}