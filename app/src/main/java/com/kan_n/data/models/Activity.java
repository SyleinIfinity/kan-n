package com.kan_n.data.models;

public class Activity {
    private String content;
    private long timestamp;

    public Activity() { } // Constructor rỗng cho Firebase

    public Activity(String content, long timestamp) {
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}