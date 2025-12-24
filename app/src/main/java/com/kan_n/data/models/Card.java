// Đặt tại: app/src/main/java/com/kan_n/data/models/Card.java

package com.kan_n.data.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

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
    @Deprecated
    private Map<String, Boolean> tagIds = new HashMap<>();

    private String selfTagId;
    private String assignedTagId;

    private String selfTagColor;     // Màu của Tag tự tạo (Gốc)
    private String assignedTagColor; // Màu của Tag được gán (Mượn)

    private boolean isCompleted; // Cho ic_tron_v1
    private String coverImageUrl;  // Cho ảnh bìa (background)
    private int attachmentCount;   // Cho ic_dinhkem
    private int checklistTotal;    // Cho ic_checked (tổng số)
    private int checklistCompleted;// Cho ic_checked (đã hoàn thành)
    private String labelColor;
    private long startDate;

    private List<String> attachmentUrls;
    private List<CheckItem> checkList;

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public List<String> getAttachmentUrls() { return attachmentUrls; }
    public void setAttachmentUrls(List<String> attachmentUrls) { this.attachmentUrls = attachmentUrls; }

    public List<CheckItem> getCheckList() { return checkList; }
    public void setCheckList(List<CheckItem> checkList) { this.checkList = checkList; }

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
        this.isCompleted = false;
        this.coverImageUrl = null;
        this.attachmentCount = 0;
        this.checklistTotal = 0;
        this.checklistCompleted = 0;
        this.selfTagId = null;
        this.assignedTagId = null;
        this.labelColor = ""; // Mặc định không màu
        this.selfTagColor = "";
        this.assignedTagColor = "";
    }

    // --- Getters and Setters ---

    @Exclude
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getListId() { return listId; }
    public void setListId(String listId) { this.listId = listId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPosition() { return position; }
    public void setPosition(double position) { this.position = position; }
    public long getDueDate() { return dueDate; }
    public void setDueDate(long dueDate) { this.dueDate = dueDate; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public Map<String, Boolean> getTagIds() { return tagIds; }
    public void setTagIds(Map<String, Boolean> tagIds) { this.tagIds = tagIds; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public int getAttachmentCount() { return attachmentCount; }
    public void setAttachmentCount(int attachmentCount) { this.attachmentCount = attachmentCount; }

    public int getChecklistTotal() { return checklistTotal; }
    public void setChecklistTotal(int checklistTotal) { this.checklistTotal = checklistTotal; }

    public int getChecklistCompleted() { return checklistCompleted; }
    public void setChecklistCompleted(int checklistCompleted) { this.checklistCompleted = checklistCompleted; }

    public String getLabelColor() { return labelColor; }
    public void setLabelColor(String labelColor) { this.labelColor = labelColor; }

    public String getSelfTagId() { return selfTagId; }
    public void setSelfTagId(String selfTagId) { this.selfTagId = selfTagId; }

    public String getAssignedTagId() { return assignedTagId; }
    public void setAssignedTagId(String assignedTagId) { this.assignedTagId = assignedTagId; }

    public String getSelfTagColor() { return selfTagColor; }
    public void setSelfTagColor(String selfTagColor) { this.selfTagColor = selfTagColor; }

    public String getAssignedTagColor() { return assignedTagColor; }
    public void setAssignedTagColor(String assignedTagColor) { this.assignedTagColor = assignedTagColor; }

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
        result.put("isCompleted", isCompleted);
        result.put("coverImageUrl", coverImageUrl);
        result.put("attachmentCount", attachmentCount);
        result.put("checklistTotal", checklistTotal);
        result.put("checklistCompleted", checklistCompleted);
        result.put("selfTagId", selfTagId);
        result.put("assignedTagId", assignedTagId);
        result.put("labelColor", labelColor);
        result.put("selfTagColor", selfTagColor);
        result.put("assignedTagColor", assignedTagColor);
        return result;
    }


}