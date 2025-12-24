package com.kan_n.data.models;

public class CheckItem {
    private String title;
    private boolean isChecked;

    public CheckItem() {} // Constructor rỗng cho Firebase

    public CheckItem(String title, boolean isChecked) {
        this.title = title;
        this.isChecked = isChecked;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }
}