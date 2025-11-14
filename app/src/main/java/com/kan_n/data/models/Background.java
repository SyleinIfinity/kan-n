package com.kan_n.data.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Background {
    private String type; // "color" hoặc "image"
    private String value; // Mã màu (#RRGGBB) hoặc URL ảnh

    // Constructor trống, getter, setter
    public Background() {}

    public Background(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}