package com.kan_n;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    private static final String DATABASE_URL = "https://kan-n-54cbf-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    public void onCreate() {
        super.onCreate();

        // Tạo FirebaseOptions hoàn toàn bằng tay
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("1:922959775793:android:e7f32785cd0687c535ad10") // Lấy từ Firebase console
                .setApiKey("AIzaSyBSk8chwLvXpgUd-ivV4jZFsGUfwEw9jMM") // Lấy từ Firebase console
                .setDatabaseUrl(DATABASE_URL)
                .setProjectId("kan-n-54cbf") // Lấy từ Firebase console
                .build();

        // Khởi tạo app DEFAULT nếu chưa có
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp app = FirebaseApp.initializeApp(this, options);
            FirebaseDatabase.getInstance(app).setPersistenceEnabled(true);
        }
    }
}
