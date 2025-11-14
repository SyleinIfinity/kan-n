package com.kan_n.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent; // <-- Thêm import
import android.os.Bundle;
import com.kan_n.R;
import com.kan_n.utils.FirebaseUtils; // <-- Thêm import

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✨ --- LOGIC ROUTING MỚI --- ✨
        // Kiểm tra xem user đã đăng nhập từ phiên trước chưa
        if (FirebaseUtils.isLoggedIn()) {
            // Nếu ĐÃ đăng nhập, đi thẳng tới MainActivity
            goToMainActivity();
        } else {
            // Nếu CHƯA đăng nhập, hiển thị màn hình chào mừng/đăng nhập
            setupAuthFlow();
        }
    }

    /**
     * Hàm này được gọi khi user CHƯA đăng nhập.
     * Nó sẽ hiển thị KhoiDongFragment (thông qua NavHost).
     */
    private void setupAuthFlow() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_auth);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}