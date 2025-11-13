package com.kan_n;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    // ✨ URL ĐÃ ĐƯỢC SỬA CHỮA (kan-n-54cbf) ✨
    private static final String DATABASE_URL = "https://kan-n-54cbf-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    public void onCreate() {
        super.onCreate();

        if (FirebaseApp.getApps(this).isEmpty()) {

            // 1. Lấy cấu hình mặc định từ google-services.json
            FirebaseOptions defaultOptions = FirebaseOptions.fromResource(this);

            // 2. Tạo một cấu hình MỚI, ghi đè URL chính xác
            FirebaseOptions customOptions = new FirebaseOptions.Builder(defaultOptions)
                    .setDatabaseUrl(DATABASE_URL)
                    .build();

            // 3. Khởi tạo ứng dụng [DEFAULT] với cấu hình MỚI này
            FirebaseApp.initializeApp(this, customOptions);

            // 4. Bật persistence cho database [DEFAULT] đã được cấu hình đúng
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
    }
}